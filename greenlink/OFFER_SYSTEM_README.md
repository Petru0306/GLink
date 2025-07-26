# Enhanced Offer Card System - Real-time Marketplace Chat

This document provides a complete guide to the enhanced offer card system that fixes race conditions, state desync issues, and provides robust real-time updates.

## 🚀 Features

- **Optimistic Locking**: Prevents race conditions with `@Version` annotation
- **Real-time Updates**: Event-driven WebSocket architecture with STOMP
- **State Management**: Centralized frontend state management
- **Offer Expiration**: Automatic expiration after 7 days
- **Counter-offer Chain**: Proper linking between original and counter offers
- **Debounced Actions**: Prevents rapid successive clicks
- **Comprehensive Testing**: Unit, integration, and WebSocket tests

## 📋 Architecture Overview

### Backend Components

1. **Enhanced Message Entity** (`Message.java`)
   - Optimistic locking with `@Version`
   - Offer expiration tracking
   - Offer chain references (original/counter offers)
   - Enhanced status enum

2. **Event System** (`OfferEvent.java`)
   - Event-driven updates
   - Real-time state synchronization
   - Comprehensive event types

3. **WebSocket Controller** (`WebSocketController.java`)
   - STOMP-based messaging
   - Event broadcasting
   - Optimistic update handling

4. **Service Layer** (`MessageServiceImpl.java`)
   - Transactional consistency
   - Optimistic locking implementation
   - Proper error handling

### Frontend Components

1. **State Manager** (`offer-state-manager.js`)
   - Centralized offer state
   - Debounced actions
   - Optimistic UI updates
   - Server reconciliation

2. **Enhanced Templates** (`conversation.html`)
   - Real-time event handling
   - State manager integration
   - Improved UX with processing states

## 🔧 Installation & Setup

### 1. Database Migration

Run the new migration to add enhanced offer fields:

```bash
# The migration will be applied automatically on startup
# V26__enhance_offer_system.sql adds:
# - version column for optimistic locking
# - offer_expires_at for expiration tracking
# - counter_offer_message_id and original_offer_message_id for offer chains
# - Indexes for better performance
```

### 2. Backend Changes

The following files have been enhanced:

- `src/main/java/com/greenlink/greenlink/model/Message.java`
- `src/main/java/com/greenlink/greenlink/dto/MessageDto.java`
- `src/main/java/com/greenlink/greenlink/dto/ChatMessageDto.java`
- `src/main/java/com/greenlink/greenlink/dto/OfferEvent.java`
- `src/main/java/com/greenlink/greenlink/service/MessageServiceImpl.java`
- `src/main/java/com/greenlink/greenlink/controller/WebSocketController.java`

### 3. Frontend Changes

Add the new JavaScript file:

```html
<!-- Add to conversation.html -->
<script src="/js/offer-state-manager.js"></script>
```

### 4. Testing

Run the comprehensive test suite:

```bash
# Run all tests
mvn test

# Run specific test classes
mvn test -Dtest=OfferServiceIntegrationTest
mvn test -Dtest=WebSocketOfferTest

# Run with coverage
mvn test jacoco:report
```

## 🧪 Testing the Real-time Features

### Local Development Testing

1. **Start the application**:
   ```bash
   mvn spring-boot:run
   ```

2. **Open two browser windows** and log in as different users

3. **Navigate to the same conversation** in both windows

4. **Test scenarios**:
   - User A makes an offer → User B sees it instantly
   - User B accepts/rejects/counters → User A sees update instantly
   - Rapid clicking → Only one action processes (debounced)
   - Network interruption → Graceful fallback to AJAX

### WebSocket Testing

Use the provided test client:

```bash
# Run WebSocket tests
mvn test -Dtest=WebSocketOfferTest
```

### Manual Testing Checklist

- [ ] Offer creation shows real-time to both users
- [ ] Accept/Reject/Counter actions update both sides instantly
- [ ] Rapid clicking doesn't cause duplicate actions
- [ ] Counter-offers create proper offer chains
- [ ] Expired offers cannot be acted upon
- [ ] Network disconnection shows appropriate UI feedback
- [ ] Reconnection restores real-time functionality

## 🔄 State Transitions

### Offer Status Flow

```
PENDING → ACTION_REQUIRED → ACCEPTED/REJECTED/COUNTERED
    ↓
EXPIRED (after 7 days)
```

### Counter-offer Flow

```
Original Offer (ACTION_REQUIRED)
    ↓
Counter Offer (ACTION_REQUIRED) ← New message with reference
    ↓
Original Offer (COUNTERED)
```

## 🛡️ Error Handling

### Optimistic Locking

- **Race Condition**: When two users act simultaneously, only one succeeds
- **User Feedback**: Clear error message: "This offer was modified by another user. Please refresh and try again."
- **Automatic Recovery**: Frontend state manager reconciles with server

### Network Issues

- **WebSocket Disconnection**: Automatic fallback to AJAX polling
- **Reconnection**: Automatic WebSocket reconnection with exponential backoff
- **State Sync**: Server state always takes precedence

### Validation

- **Expired Offers**: Cannot be acted upon
- **Own Offers**: Cannot respond to your own offers
- **Already Processed**: Cannot act on already processed offers
- **Invalid Actions**: Proper error messages for invalid actions

## 📊 Monitoring & Observability

### Logging

Key events are logged with appropriate levels:

```java
// Offer creation
logger.info("Offer sent via WebSocket: " + responseMessage.getOfferAmount());

// Optimistic locking failures
logger.log(Level.WARNING, "Optimistic locking failure for offer response: " + messageId, e);

// Real-time events
logger.info("Status update sent via WebSocket for message " + messageId + " with status: " + status);
```

### Metrics to Monitor

- WebSocket connection success rate
- Optimistic locking failure rate
- Offer response times
- Counter-offer frequency
- Expired offer cleanup

## 🚀 Performance Considerations

### Database Optimization

- Indexes on `offer_status`, `offer_expires_at`, and offer chain references
- Optimistic locking prevents unnecessary database locks
- Efficient queries with proper joins

### Frontend Optimization

- Debounced actions prevent excessive server requests
- State manager prevents unnecessary DOM updates
- WebSocket connection pooling and reconnection logic

### Scalability

- Event-driven architecture supports horizontal scaling
- WebSocket topics can be distributed across multiple servers
- Database indexes support high-volume offer processing

## 🔧 Configuration

### Application Properties

```properties
# WebSocket configuration
spring.websocket.max-text-message-size=8192
spring.websocket.max-binary-message-size=8192

# Database connection pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# Logging
logging.level.com.greenlink.greenlink.service=DEBUG
logging.level.org.springframework.messaging=DEBUG
```

### Environment Variables

```bash
# Database
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/greenlink
JDBC_DATABASE_USERNAME=postgres
JDBC_DATABASE_PASSWORD=password

# WebSocket
WEBSOCKET_ENABLED=true
WEBSOCKET_HEARTBEAT_INTERVAL=30000
```

## 🐛 Troubleshooting

### Common Issues

1. **WebSocket Connection Fails**
   - Check CSRF configuration
   - Verify WebSocket endpoint is accessible
   - Check browser console for errors

2. **Optimistic Locking Errors**
   - Normal behavior for concurrent actions
   - User should refresh and retry
   - Check for proper error handling in UI

3. **State Desync**
   - Verify state manager initialization
   - Check WebSocket event handling
   - Ensure proper event type mapping

4. **Counter-offer Issues**
   - Verify offer chain references
   - Check database constraints
   - Validate event broadcasting

### Debug Mode

Enable debug logging:

```properties
logging.level.com.greenlink.greenlink=DEBUG
logging.level.org.springframework.messaging=DEBUG
logging.level.org.springframework.web.socket=DEBUG
```

## 📈 Future Enhancements

- **Push Notifications**: Mobile push for offer updates
- **Offer Analytics**: Track offer success rates and patterns
- **Auto-expiration**: Background job to expire old offers
- **Bulk Operations**: Handle multiple offers simultaneously
- **Advanced Negotiation**: Multi-round counter-offer chains

## 🤝 Contributing

When contributing to the offer system:

1. **Follow the existing patterns** for state management and event handling
2. **Add comprehensive tests** for new features
3. **Update this README** with new functionality
4. **Test real-time behavior** with multiple browser windows
5. **Consider performance implications** of changes

## 📞 Support

For issues or questions:

1. Check the troubleshooting section above
2. Review the test cases for expected behavior
3. Enable debug logging for detailed analysis
4. Test with the provided WebSocket test client

---

**Note**: This enhanced offer system provides a robust, scalable foundation for real-time marketplace negotiations with proper error handling, state management, and comprehensive testing. 