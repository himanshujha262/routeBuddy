package com.routbuddy.drivers;

import com.routbuddy.common.domain.enums.KYCStatus;
import com.routbuddy.common.domain.enums.UserRole;
import com.routbuddy.common.exception.BadRequestException;
import com.routbuddy.common.exception.UserAlreadyExistsException;
import com.routbuddy.drivers.domain.entity.DriverProfile;
import com.routbuddy.drivers.dto.DriverProfileDto;
import com.routbuddy.drivers.dto.RegisterDriverRequest;
import com.routbuddy.drivers.dto.UpdateKycRequest;
import com.routbuddy.drivers.repository.DriverRepository;
import com.routbuddy.drivers.service.DriverService;
import com.routbuddy.drivers.service.DriverServiceImpl;
import com.routbuddy.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private UserService userService;

    private DriverService driverService;

    private UUID testUserId;
    private UUID testDriverId;
    private RegisterDriverRequest testRequest;
    private DriverProfile testProfile;

    @BeforeEach
    void setUp() {
        driverService = new DriverServiceImpl(driverRepository, userService);
        testUserId = UUID.randomUUID();
        testDriverId = UUID.randomUUID();

        testRequest = RegisterDriverRequest.builder()
                .licenseNumber("DL-1420110012345")
                .licenseExpiryDate(LocalDate.now().plusYears(5))
                .licenseFrontImageUrl("https://cdn.routbuddy.com/licenses/front.jpg")
                .licenseBackImageUrl("https://cdn.routbuddy.com/licenses/back.jpg")
                .aadhaarMasked("XXXX-XXXX-1234")
                .build();

        testProfile = DriverProfile.builder()
                .id(testDriverId)
                .userId(testUserId)
                .licenseNumber("DL-1420110012345")
                .licenseExpiryDate(LocalDate.now().plusYears(5))
                .kycStatus(KYCStatus.PENDING)
                .ratingAvg(5.0)
                .online(false)
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new driver profile")
    void testRegisterDriver_Success() {
        when(driverRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
        when(driverRepository.findByLicenseNumber("DL-1420110012345")).thenReturn(Optional.empty());
        when(driverRepository.save(any(DriverProfile.class))).thenAnswer(invocation -> {
            DriverProfile p = invocation.getArgument(0);
            p.setId(testDriverId);
            return p;
        });

        DriverProfileDto dto = driverService.registerDriver(testUserId, testRequest);

        assertNotNull(dto);
        assertEquals(testDriverId, dto.getId());
        assertEquals("DL-1420110012345", dto.getLicenseNumber());
        assertEquals(KYCStatus.PENDING, dto.getKycStatus());
        verify(userService, times(1)).assignRoleToUser(testUserId, UserRole.DRIVER);
        verify(driverRepository, times(1)).save(any(DriverProfile.class));
    }

    @Test
    @DisplayName("Should throw exception when driver already registered for user")
    void testRegisterDriver_DuplicateUser_ThrowsException() {
        when(driverRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));

        assertThrows(UserAlreadyExistsException.class, () -> driverService.registerDriver(testUserId, testRequest));
        verify(driverRepository, never()).save(any(DriverProfile.class));
    }

    @Test
    @DisplayName("Should throw exception when license number is already registered")
    void testRegisterDriver_DuplicateLicense_ThrowsException() {
        when(driverRepository.findByUserId(testUserId)).thenReturn(Optional.empty());
        when(driverRepository.findByLicenseNumber("DL-1420110012345")).thenReturn(Optional.of(testProfile));

        assertThrows(UserAlreadyExistsException.class, () -> driverService.registerDriver(testUserId, testRequest));
        verify(driverRepository, never()).save(any(DriverProfile.class));
    }

    @Test
    @DisplayName("Should reject going online if KYC is not APPROVED")
    void testUpdateOnlineStatus_NotApproved_ThrowsException() {
        testProfile.setKycStatus(KYCStatus.PENDING);
        when(driverRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                driverService.updateOnlineStatus(testUserId, true)
        );

        assertTrue(ex.getMessage().contains("KYC must be APPROVED"));
    }

    @Test
    @DisplayName("Should allow going online when KYC is APPROVED")
    void testUpdateOnlineStatus_Approved_Success() {
        testProfile.setKycStatus(KYCStatus.APPROVED);
        when(driverRepository.findByUserId(testUserId)).thenReturn(Optional.of(testProfile));
        when(driverRepository.save(any(DriverProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverProfileDto dto = driverService.updateOnlineStatus(testUserId, true);

        assertTrue(dto.isOnline());
        verify(driverRepository, times(1)).save(testProfile);
    }

    @Test
    @DisplayName("Should update KYC status by Admin")
    void testUpdateKycStatus_Success() {
        when(driverRepository.findById(testDriverId)).thenReturn(Optional.of(testProfile));
        when(driverRepository.save(any(DriverProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateKycRequest kycReq = new UpdateKycRequest(KYCStatus.APPROVED, null);
        DriverProfileDto dto = driverService.updateKycStatus(testDriverId, kycReq);

        assertEquals(KYCStatus.APPROVED, dto.getKycStatus());
        assertNull(dto.getKycRejectionReason());
    }
}
