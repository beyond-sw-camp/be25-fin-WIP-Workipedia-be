package com.wip.workipedia.admin.manual.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wip.workipedia.admin.manual.dto.AdminManualCreateRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.repository.ManualFileRepository;
import com.wip.workipedia.manual.repository.ManualRepository;
import com.wip.workipedia.manual.repository.ManualVersionRepository;
import com.wip.workipedia.notification.service.NotificationService;
import com.wip.workipedia.storage.service.StorageService;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

	@Test
	void create_convertsActiveTitleUniqueViolationToConflict() {
		AdminManualService service = service();
		when(userRepository.findById(1L)).thenReturn(Optional.of(systemAdmin()));
		when(manualRepository.existsByTitleAndDeletedAtIsNull("manual")).thenReturn(false);
		when(manualRepository.saveAndFlush(any(Manual.class)))
			.thenThrow(new DataIntegrityViolationException("duplicate active manual title"));

		assertThatThrownBy(() -> service.create(
			1L,
			new AdminManualCreateRequest(null, "manual", "content", ManualStatus.PUBLISHED, null)
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
			.thenThrow(new DataIntegrityViolationException("duplicate active manual title"));

		assertThatThrownBy(() -> service.createFromPdf(
			1L,
			null,
			"manual",
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
		org.mockito.Mockito.doThrow(new DataIntegrityViolationException("duplicate active manual title"))
			.when(manualRepository)
			.flush();

		assertThatThrownBy(() -> service.updateFromPdf(
			1L,
			100L,
			null,
			"manual",
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
			Runnable::run
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
