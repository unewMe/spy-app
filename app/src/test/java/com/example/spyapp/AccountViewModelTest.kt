package com.example.spyapp

import com.example.spyapp.api.InvitationRepository
import com.example.spyapp.api.PartnersRepository
import com.example.spyapp.models.Invitation
import com.example.spyapp.models.Partner
import com.example.spyapp.models.User
import com.example.spyapp.viewmodels.AccountViewModel
import com.example.spyapp.viewmodels.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AccountViewModelTest {
    private lateinit var viewModel: AccountViewModel
    private lateinit var partnersRepository: PartnersRepository
    private lateinit var invitationRepository: InvitationRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        partnersRepository = mock<PartnersRepository>()
        invitationRepository = mock<InvitationRepository>()

        val mockUser = User(id = "test-user-id", email = "user@example.com")

        whenever(partnersRepository.getPartners()).thenReturn(flowOf(emptyList()))
        whenever(invitationRepository.getInvitations()).thenReturn(flowOf(emptyList()))

        viewModel = AccountViewModel(partnersRepository, invitationRepository, mockUser) // Pass mockUser here
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCurrentUser populates currentUser`() = testScope.runTest {
        // advanceUntilIdle() // No longer needed if user is passed in constructor
        assertEquals("test-user-id", viewModel.currentUser.value?.id)
    }

    @Test
    fun `loadPartners populates partners`() = testScope.runTest {
        val expectedPartners = listOf(Partner(id = "partner1", userId = "user1", partnerId = "partnerUser1", partnerEmail = "partner@example.com"))
        whenever(partnersRepository.getPartners()).thenReturn(flowOf(expectedPartners))
        val mockUser = User(id = "test-user-id", email = "user@example.com")

        // Re-initialize viewModel or trigger loadPartners if it's not in init
        viewModel = AccountViewModel(partnersRepository, invitationRepository, mockUser)
        advanceUntilIdle()

        assertEquals(expectedPartners, viewModel.partners.value)
    }

    @Test
    fun `loadInvitations populates invitations`() = testScope.runTest {
        val expectedInvitations = listOf(Invitation(id = "inv1", fromUserId = "user2", fromEmail = "test@example.com", toEmail = "user@example.com", timestamp = System.currentTimeMillis()))
        whenever(invitationRepository.getInvitations()).thenReturn(flowOf(expectedInvitations))
        val mockUser = User(id = "test-user-id", email = "user@example.com")

        // Re-initialize viewModel or trigger loadInvitations if it's not in init
        viewModel = AccountViewModel(partnersRepository, invitationRepository, mockUser)
        advanceUntilIdle()

        assertEquals(expectedInvitations, viewModel.invitations.value)
    }

    @Test
    fun `addPartner calls sendInvitation and handles success`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val email = "partner@example.com"

        viewModel.addPartner(email) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        verify(invitationRepository).sendInvitation(email)
        assertTrue("Success callback should be called", successCalled)
        assertNull("Error message should be null", errorMessage)
    }

    @Test
    fun `addPartner handles failure`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val email = "partner@example.com"
        val exception = RuntimeException("Send failed")
        whenever(invitationRepository.sendInvitation(email)).thenThrow(exception)

        viewModel.addPartner(email) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        assertFalse("Success callback should not be called", successCalled)
        assertEquals("Error message should match", "Send failed", errorMessage)
    }

    @Test
    fun `acceptInvitation calls acceptInvitation and handles success`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val invitation = Invitation(id = "inv1", fromUserId = "user2", fromEmail = "test@example.com", toEmail = "user@example.com", timestamp = System.currentTimeMillis())

        viewModel.acceptInvitation(invitation) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        verify(invitationRepository).acceptInvitation(invitation)
        assertTrue("Success callback should be called", successCalled)
        assertNull("Error message should be null", errorMessage)
    }

    @Test
    fun `acceptInvitation handles failure`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val invitation = Invitation(id = "inv1", fromUserId = "user2", fromEmail = "test@example.com", toEmail = "user@example.com", timestamp = System.currentTimeMillis())
        val exception = RuntimeException("Accept failed")
        whenever(invitationRepository.acceptInvitation(invitation)).thenThrow(exception)

        viewModel.acceptInvitation(invitation) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        assertFalse("Success callback should not be called", successCalled)
        assertEquals("Error message should match", "Accept failed", errorMessage)
    }

    @Test
    fun `rejectInvitation calls rejectInvitation and handles success`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val invitation = Invitation(id = "inv1", fromUserId = "user2", fromEmail = "test@example.com", toEmail = "user@example.com", timestamp = System.currentTimeMillis())

        viewModel.rejectInvitation(invitation) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        verify(invitationRepository).rejectInvitation(invitation)
        assertTrue("Success callback should be called", successCalled)
        assertNull("Error message should be null", errorMessage)
    }

    @Test
    fun `rejectInvitation handles failure`() = testScope.runTest {
        var successCalled = false
        var errorMessage: String? = null
        val invitation = Invitation(id = "inv1", fromUserId = "user2", fromEmail = "test@example.com", toEmail = "user@example.com", timestamp = System.currentTimeMillis())
        val exception = RuntimeException("Reject failed")
        whenever(invitationRepository.rejectInvitation(invitation)).thenThrow(exception)

        viewModel.rejectInvitation(invitation) { success, message ->
            successCalled = success
            errorMessage = message
        }

        advanceUntilIdle()

        assertFalse("Success callback should not be called", successCalled)
        assertEquals("Error message should match", "Reject failed", errorMessage)
    }
}
