package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Product;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.ProductRepository;
import com.greenlink.greenlink.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private StripeService stripeService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentService paymentService;

    private User buyer;
    private User seller;
    private Product product;

    @BeforeEach
    void setUp() {
        buyer = new User();
        buyer.setId(1L);
        buyer.setEmail("buyer@test.com");
        buyer.setFirstName("John");
        buyer.setLastName("Doe");
        buyer.setStripeCustomerId("cus_test123");

        seller = new User();
        seller.setId(2L);
        seller.setEmail("seller@test.com");
        seller.setFirstName("Jane");
        seller.setLastName("Smith");
        seller.setStripeAccountId("acct_test123");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(100.0);
        product.setSeller(seller);
        product.setSold(false);
    }

    @Test
    void testCreateCheckoutSession_Success() throws StripeException {
        // Arrange
        Session mockSession = mock(Session.class);
        when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/test");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(stripeService.createCheckoutSession(any(), any(), any(), any(), any()))
                .thenReturn(mockSession);

        // Act
        String result = paymentService.createCheckoutSession(1L, buyer, "success", "cancel");

        // Assert
        assertNotNull(result);
        assertEquals("https://checkout.stripe.com/test", result);
        verify(productRepository).findById(1L);
        verify(stripeService).createCheckoutSession(eq(product), eq(buyer), eq("success"), eq("cancel"), any());
    }

    @Test
    void testCreateCheckoutSession_ProductNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            paymentService.createCheckoutSession(1L, buyer, "success", "cancel");
        });
    }

    @Test
    void testCreateCheckoutSession_ProductAlreadySold() {
        // Arrange
        product.setSold(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            paymentService.createCheckoutSession(1L, buyer, "success", "cancel");
        });
    }

    @Test
    void testCreateCheckoutSession_CreateCustomerIfNeeded() throws StripeException {
        // Arrange
        buyer.setStripeCustomerId(null);
        Session mockSession = mock(Session.class);
        when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/test");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(stripeService.createCustomer(buyer)).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setStripeCustomerId("cus_new123");
            return "cus_new123";
        });
        when(userRepository.save(buyer)).thenReturn(buyer);
        when(stripeService.createCheckoutSession(any(), any(), any(), any(), any()))
                .thenReturn(mockSession);

        // Act
        String result = paymentService.createCheckoutSession(1L, buyer, "success", "cancel");

        // Assert
        assertNotNull(result);
        assertEquals("https://checkout.stripe.com/test", result);
        verify(stripeService).createCustomer(buyer);
        verify(userRepository).save(buyer);
        assertEquals("cus_new123", buyer.getStripeCustomerId());
    }

    @Test
    void testProcessSuccessfulPayment_Success() throws StripeException {
        // Arrange
        Session mockSession = mock(Session.class);
        when(mockSession.getMetadata()).thenReturn(java.util.Map.of(
                "product_id", "1",
                "buyer_id", "1"
        ));
        when(stripeService.retrieveSession("session_123")).thenReturn(mockSession);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(productRepository.save(any())).thenReturn(product);

        // Act
        paymentService.processSuccessfulPayment("session_123");

        // Assert
        verify(stripeService).retrieveSession("session_123");
        verify(productRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(productRepository).save(any());
        
        assertTrue(product.isSold());
        assertEquals(buyer, product.getBuyer());
        assertNotNull(product.getSoldAt());
    }

    @Test
    void testProcessSuccessfulPayment_ProductNotFound() throws StripeException {
        // Arrange
        Session mockSession = mock(Session.class);
        when(mockSession.getMetadata()).thenReturn(java.util.Map.of(
                "product_id", "1",
                "buyer_id", "1"
        ));
        when(stripeService.retrieveSession("session_123")).thenReturn(mockSession);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            paymentService.processSuccessfulPayment("session_123");
        });
    }

    @Test
    void testProcessSuccessfulPayment_BuyerNotFound() throws StripeException {
        // Arrange
        Session mockSession = mock(Session.class);
        when(mockSession.getMetadata()).thenReturn(java.util.Map.of(
                "product_id", "1",
                "buyer_id", "1"
        ));
        when(stripeService.retrieveSession("session_123")).thenReturn(mockSession);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            paymentService.processSuccessfulPayment("session_123");
        });
    }

    @Test
    void testProcessSuccessfulPayment_ProductAlreadySold() throws StripeException {
        // Arrange
        product.setSold(true);
        Session mockSession = mock(Session.class);
        when(mockSession.getMetadata()).thenReturn(java.util.Map.of(
                "product_id", "1",
                "buyer_id", "1"
        ));
        when(stripeService.retrieveSession("session_123")).thenReturn(mockSession);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            paymentService.processSuccessfulPayment("session_123");
        });
    }

    @Test
    void testProcessSuccessfulPayment_MissingMetadata() throws StripeException {
        // Arrange
        Session mockSession = mock(Session.class);
        when(mockSession.getMetadata()).thenReturn(java.util.Map.of());
        when(stripeService.retrieveSession("session_123")).thenReturn(mockSession);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            paymentService.processSuccessfulPayment("session_123");
        });
    }
} 