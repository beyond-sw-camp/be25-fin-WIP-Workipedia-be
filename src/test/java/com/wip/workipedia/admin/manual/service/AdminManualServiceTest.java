package com.wip.workipedia.admin.manual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wip.workipedia.admin.manual.dto.AdminManualCreateRequest;
import com.wip.workipedia.admin.manual.dto.AdminManualUpdateRequest;
import com.wip.workipedia.aisync.service.AiSyncJobService;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualFile;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.domain.ManualVersion;
import com.wip.workipedia.manual.dto.ManualDetailResponse;
import com.wip.workipedia.manual.repository.ManualFileRepository;
import com.wip.workipedia.manual.repository.ManualRepository;
import com.wip.workipedia.manual.repository.ManualVersionRepository;
import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.storage.dto.StoredObject;
import com.wip.workipedia.storage.service.StorageService;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AdminManualServiceTest {

	@Mock
	private ManualRepository manualRepository;

	@Mock
	private ManualFileRepository manualFileRepository;

	@Mock
	private ManualVersionRepository manualVersionRepository;

	@Mock
	private DepartmentRepository departmentRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private NotificationService notificationService;

	@Mock
	private ManualPdfValidator manualPdfValidator;

	@Mock
	private PdfTextExtractor pdfTextExtractor;

	@Mock
	private StorageService storageService;

	@Mock
	private AiSyncJobService aiSyncJobService;

	@Test
	void create_convertsActiveTitleUniqueViolationToConflict() {
		AdminManualService service = service();
		when(userRepository.findById(1L)).thenReturn(Optional.of(systemAdmin()));
		when(manualRepository.existsByTitleAndDeletedAtIsNull("manual")).thenReturn(false);
		when(manualRepository.saveAndFlush(any(Manual.class)))
			.thenThrow(activeTitleViolation());

		assertThatThrownBy(() -> service.create(
			1L,
			new AdminManualCreateRequest(null, "manual", "content", null, ManualStatus.PUBLISHED, null)
		))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.CONFLICT);
		verify(manualVersionRepository, never()).save(any());
	}

	@Test
	void createFromPdf_convertsActiveTitleUniqueViolationToConflictBeforeUploadingPdf() {
		AdminManualService service = service();
		MultipartFile file = mock(MultipartFile.class);
		byte[] pdfBytes = "pdf".getBytes();
		when(userRepository.findById(1L)).thenReturn(Optional.of(systemAdmin()));
		when(manualRepository.existsByTitleAndDeletedAtIsNull("manual")).thenReturn(false);
		when(manualPdfValidator.validateAndRead(file)).thenReturn(pdfBytes);
		when(pdfTextExtractor.extract(file, pdfBytes)).thenReturn("content");
		when(manualRepository.saveAndFlush(any(Manual.class)))
			.thenThrow(activeTitleViolation());

		assertThatThrownBy(() -> service.createFromPdf(
			1L,
			null,
			"manual",
			null,
			ManualStatus.PUBLISHED,
			null,
			List.of(file)
		))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.CONFLICT);
		verify(storageService, never()).upload(any(), any(), any(), any());
		verify(manualFileRepository, never()).save(any());
		verify(manualVersionRepository, never()).save(any());
	}

	@Test
	void updateFromPdf_convertsActiveTitleUniqueViolationToConflictBeforeUploadingPdf() {
		AdminManualService service = service();
		Manual manual = Manual.create(null, "old title", "old content", ManualStatus.PUBLISHED, null, "1.0", 1L);
		ReflectionTestUtils.setField(manual, "manualId", 100L);
		MultipartFile file = mock(MultipartFile.class);
		byte[] pdfBytes = "pdf".getBytes();
		when(userRepository.findById(1L)).thenReturn(Optional.of(systemAdmin()));
		when(manualRepository.findByManualIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(manual));
		when(manualRepository.existsByTitleAndManualIdNotAndDeletedAtIsNull("manual", 100L)).thenReturn(false);
		when(manualPdfValidator.validateAndRead(file)).thenReturn(pdfBytes);
		when(pdfTextExtractor.extract(file, pdfBytes)).thenReturn("content");
		when(manualVersionRepository.findTopByManualManualIdAndDeletedAtIsNullOrderByManualVersionIdDesc(100L))
			.thenReturn(Optional.empty());
		when(manualFileRepository.countByManualManualIdAndDeletedAtIsNull(100L)).thenReturn(0L);
		org.mockito.Mockito.doThrow(activeTitleViolation())
			.when(manualRepository)
			.flush();

		assertThatThrownBy(() -> service.updateFromPdf(
			1L,
			100L,
			null,
			"manual",
			null,
			ManualStatus.PUBLISHED,
			null,
			List.of(file)
		))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.CONFLICT);
		verify(storageService, never()).upload(any(), any(), any(), any());
		verify(manualFileRepository, never()).save(any());
		verify(manualVersionRepository, never()).save(any());
	}

	@Test
	void updateFromPdf_incrementsMajorVersionWhenPdfFileCountChanges() {
		AdminManualService service = service();
		Manual manual = Manual.create(null, "old title", "old content", ManualStatus.PUBLISHED, null, "1.9", 1L);
		ReflectionTestUtils.setField(manual, "manualId", 100L);
		MultipartFile firstFile = mock(MultipartFile.class);
		MultipartFile secondFile = mock(MultipartFile.class);
		when(firstFile.getOriginalFilename()).thenReturn("first.pdf");
		when(secondFile.getOriginalFilename()).thenReturn("second.pdf");
		when(userRepository.findById(1L)).thenReturn(Optional.of(systemAdmin()));
		when(manualRepository.findByManualIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(manual));
		when(manualRepository.existsByTitleAndManualIdNotAndDeletedAtIsNull("manual", 100L)).thenReturn(false);
		when(manualPdfValidator.validateAndRead(firstFile)).thenReturn("first".getBytes());
		when(manualPdfValidator.validateAndRead(secondFile)).thenReturn("second".getBytes());
		when(pdfTextExtractor.extract(firstFile, "first".getBytes())).thenReturn("first content");
		when(pdfTextExtractor.extract(secondFile, "second".getBytes())).thenReturn("second content");
		when(manualVersionRepository.findTopByManualManualIdAndDeletedAtIsNullOrderByManualVersionIdDesc(100L))
			.thenReturn(Optional.empty());
		when(manualFileRepository.countByManualManualIdAndDeletedAtIsNull(100L)).thenReturn(1L);
		when(manualVersionRepository.existsByManualManualIdAndManualNumAndDeletedAtIsNull(100L, "2.0"))
			.thenReturn(false);
		when(storageService.upload(any(), any(), any(), any()))
			.thenReturn(new StoredObject("first-key", "first-url"), new StoredObject("second-key", "second-url"));
		when(manualVersionRepository.save(any(ManualVersion.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		ManualDetailResponse response = service.updateFromPdf(
			1L,
			100L,
			null,
			"manual",
			null,
			ManualStatus.PUBLISHED,
			null,
			List.of(firstFile, secondFile)
		);

		assertThat(response.version()).isEqualTo("2.0");
		assertThat(response.fileUrls()).containsExactly("first-url", "second-url");
		verify(manualFileRepository, times(2)).save(any(ManualFile.class));
		ArgumentCaptor<ManualVersion> versionCaptor = ArgumentCaptor.forClass(ManualVersion.class);
		verify(manualVersionRepository).save(versionCaptor.capture());
		assertThat(versionCaptor.getValue().getManualNum()).isEqualTo("2.0");
		assertThat(versionCaptor.getValue().getUpdateReason()).isEqualTo("FILE_ADDED");
		assertThat(versionCaptor.getValue().getContentDiff()).isNull();
	}

	@Test
	void createFromPdf_savesEveryUploadedPdfFileInOneManual() {
		AdminManualService service = service();
		MultipartFile firstFile = mock(MultipartFile.class);
		MultipartFile secondFile = mock(MultipartFile.class);
		when(firstFile.getOriginalFilename()).thenReturn("first.pdf");
		when(secondFile.getOriginalFilename()).thenReturn("second.pdf");
		when(userRepository.findById(1L)).thenReturn(Optional.of(systemAdmin()));
		when(manualRepository.existsByTitleAndDeletedAtIsNull("manual")).thenReturn(false);
		when(manualPdfValidator.validateAndRead(firstFile)).thenReturn("first".getBytes());
		when(manualPdfValidator.validateAndRead(secondFile)).thenReturn("second".getBytes());
		when(pdfTextExtractor.extract(firstFile, "first".getBytes())).thenReturn("first content");
		when(pdfTextExtractor.extract(secondFile, "second".getBytes())).thenReturn("second content");
		when(manualRepository.saveAndFlush(any(Manual.class)))
			.thenAnswer(invocation -> {
				Manual manual = invocation.getArgument(0);
				ReflectionTestUtils.setField(manual, "manualId", 100L);
				return manual;
			});
		when(storageService.upload(any(), any(), any(), any()))
			.thenReturn(new StoredObject("first-key", "first-url"), new StoredObject("second-key", "second-url"));
		when(manualVersionRepository.existsByManualManualIdAndManualNumAndDeletedAtIsNull(100L, "1.0"))
			.thenReturn(false);
		when(manualVersionRepository.save(any(ManualVersion.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		ManualDetailResponse response = service.createFromPdf(
			1L,
			null,
			"manual",
			"manual description",
			ManualStatus.PUBLISHED,
			null,
			List.of(firstFile, secondFile)
		);

		assertThat(response.description()).isEqualTo("manual description");
		assertThat(response.content()).contains("first content", "second content");
		assertThat(response.fileUrls()).containsExactly("first-url", "second-url");
		verify(manualFileRepository, times(2)).save(any(ManualFile.class));
	}

	@Test
	void delete_clearsRepresentativeFileAndExcludesManualFromActiveDuplicates() {
		AdminManualService service = service();
		Manual manual = Manual.create(null, "manual", "content", ManualStatus.PUBLISHED, null, "1.0", 1L);
		ReflectionTestUtils.setField(manual, "manualId", 100L);
		manual.attachFile("manual-key", "manual-url");
		when(userRepository.findById(1L)).thenReturn(Optional.of(systemAdmin()));
		when(manualRepository.findByManualIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(manual));

		service.delete(1L, 100L);

		assertThat(manual.getDeletedAt()).isNotNull();
		assertThat(manual.getFileKey()).isNull();
		assertThat(manual.getFileUrl()).isNull();
		verify(manualFileRepository).findByManualManualIdAndDeletedAtIsNullOrderBySortOrderAsc(100L);
	}

	@Test
	void updateFromPdf_incrementsMinorVersionWhenPdfFileCountIsSame() {
		AdminManualService service = service();
		Manual manual = Manual.create(null, "old title", "old content", ManualStatus.PUBLISHED, null, "1.4", 1L);
		ReflectionTestUtils.setField(manual, "manualId", 100L);
		MultipartFile file = mock(MultipartFile.class);
		when(file.getOriginalFilename()).thenReturn("manual.pdf");
		when(userRepository.findById(1L)).thenReturn(Optional.of(systemAdmin()));
		when(manualRepository.findByManualIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(manual));
		when(manualRepository.existsByTitleAndManualIdNotAndDeletedAtIsNull("manual", 100L)).thenReturn(false);
		when(manualPdfValidator.validateAndRead(file)).thenReturn("changed".getBytes());
		when(pdfTextExtractor.extract(file, "changed".getBytes())).thenReturn("changed content");
		when(manualVersionRepository.findTopByManualManualIdAndDeletedAtIsNullOrderByManualVersionIdDesc(100L))
			.thenReturn(Optional.empty());
		when(manualFileRepository.countByManualManualIdAndDeletedAtIsNull(100L)).thenReturn(1L);
		when(manualVersionRepository.existsByManualManualIdAndManualNumAndDeletedAtIsNull(100L, "1.5"))
			.thenReturn(false);
		when(storageService.upload(any(), any(), any(), any()))
			.thenReturn(new StoredObject("manual-key", "manual-url"));
		when(manualVersionRepository.save(any(ManualVersion.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		ManualDetailResponse response = service.updateFromPdf(
			1L,
			100L,
			null,
			"manual",
			null,
			ManualStatus.PUBLISHED,
			null,
			List.of(file)
		);

		assertThat(response.version()).isEqualTo("1.5");
		ArgumentCaptor<ManualVersion> versionCaptor = ArgumentCaptor.forClass(ManualVersion.class);
		verify(manualVersionRepository).save(versionCaptor.capture());
		assertThat(versionCaptor.getValue().getManualNum()).isEqualTo("1.5");
		assertThat(versionCaptor.getValue().getContentDiff()).contains("- old content", "+ changed content");
	}

	@Test
	void updateFromPdf_skipsVersionAndUploadWhenPdfContentAndFileCountAreSame() {
		AdminManualService service = service();
		Manual manual = Manual.create(null, "old title", "same content", ManualStatus.PUBLISHED, null, "1.4", 1L);
		ReflectionTestUtils.setField(manual, "manualId", 100L);
		MultipartFile file = mock(MultipartFile.class);
		when(userRepository.findById(1L)).thenReturn(Optional.of(systemAdmin()));
		when(manualRepository.findByManualIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(manual));
		when(manualPdfValidator.validateAndRead(file)).thenReturn("same".getBytes());
		when(pdfTextExtractor.extract(file, "same".getBytes())).thenReturn("same content");
		when(manualFileRepository.countByManualManualIdAndDeletedAtIsNull(100L)).thenReturn(1L);

		ManualDetailResponse response = service.updateFromPdf(
			1L,
			100L,
			null,
			null,
			null,
			null,
			null,
			List.of(file)
		);

		assertThat(response.version()).isEqualTo("1.4");
		verify(manualVersionRepository, never()).save(any());
		verify(storageService, never()).upload(any(), any(), any(), any());
		verify(manualRepository, never()).flush();
	}

	@Test
	void updateFromPdf_storesLineDiffWhenOneCharacterChanges() {
		AdminManualService service = service();
		Manual manual = Manual.create(null, "old title", "hello world", ManualStatus.PUBLISHED, null, "1.4", 1L);
		ReflectionTestUtils.setField(manual, "manualId", 100L);
		MultipartFile file = mock(MultipartFile.class);
		when(file.getOriginalFilename()).thenReturn("manual.pdf");
		when(userRepository.findById(1L)).thenReturn(Optional.of(systemAdmin()));
		when(manualRepository.findByManualIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(manual));
		when(manualPdfValidator.validateAndRead(file)).thenReturn("changed".getBytes());
		when(pdfTextExtractor.extract(file, "changed".getBytes())).thenReturn("hello worle");
		when(manualVersionRepository.findTopByManualManualIdAndDeletedAtIsNullOrderByManualVersionIdDesc(100L))
			.thenReturn(Optional.empty());
		when(manualFileRepository.countByManualManualIdAndDeletedAtIsNull(100L)).thenReturn(1L);
		when(manualVersionRepository.existsByManualManualIdAndManualNumAndDeletedAtIsNull(100L, "1.5"))
			.thenReturn(false);
		when(storageService.upload(any(), any(), any(), any()))
			.thenReturn(new StoredObject("manual-key", "manual-url"));
		when(manualVersionRepository.save(any(ManualVersion.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		service.updateFromPdf(
			1L,
			100L,
			null,
			null,
			null,
			null,
			null,
			List.of(file)
		);

		ArgumentCaptor<ManualVersion> versionCaptor = ArgumentCaptor.forClass(ManualVersion.class);
		verify(manualVersionRepository).save(versionCaptor.capture());
		assertThat(versionCaptor.getValue().getManualNum()).isEqualTo("1.5");
		assertThat(versionCaptor.getValue().getContentDiff())
			.contains("@@ line 1 @@", "- hello world", "+ hello worle", "?           ^");
	}

	@Test
	void update_keepsVersionWhenManualContentChanges() {
		AdminManualService service = service();
		Manual manual = Manual.create(null, "old title", "old content", ManualStatus.PUBLISHED, null, "3.2", 1L);
		ReflectionTestUtils.setField(manual, "manualId", 100L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(systemAdmin()));
		when(manualRepository.findByManualIdAndDeletedAtIsNull(100L)).thenReturn(Optional.of(manual));
		when(manualRepository.existsByTitleAndManualIdNotAndDeletedAtIsNull("manual", 100L)).thenReturn(false);

		ManualDetailResponse response = service.update(
			1L,
			100L,
			new AdminManualUpdateRequest(null, "manual", "changed content", null, ManualStatus.PUBLISHED, null, "CONTENT_UPDATE")
		);

		assertThat(response.version()).isEqualTo("3.2");
		assertThat(response.content()).isEqualTo("changed content");
		verify(manualVersionRepository, never()).save(any());
	}

	private AdminManualService service() {
		return new AdminManualService(
			manualRepository,
			manualFileRepository,
			manualVersionRepository,
			departmentRepository,
			userRepository,
			notificationService,
			manualPdfValidator,
			pdfTextExtractor,
			storageService,
			Runnable::run,
			aiSyncJobService
		);
	}

	private DataIntegrityViolationException activeTitleViolation() {
		return new DataIntegrityViolationException(
			"duplicate active manual title",
			new org.hibernate.exception.ConstraintViolationException(
				"duplicate active manual title",
				new SQLException("duplicate active manual title"),
				"uk_manuals_active_title"
			)
		);
	}

	private User systemAdmin() {
		User user = User.signup(
			mock(Department.class),
			"employee-1",
			"admin@example.com",
			"password",
			"admin"
		);
		ReflectionTestUtils.setField(user, "role", UserRole.SYSTEM_ADMIN);
		return user;
	}
}
