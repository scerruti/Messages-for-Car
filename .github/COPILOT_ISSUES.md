# Native UI Refactor Issues

## Issues to Create in GitHub

Here are the issues we need to create for the native UI refactor. These can be created directly in GitHub and assigned to Copilot agents.

### Epic: Native UI Refactor

**Main Epic Issue:**
```
Title: [EPIC] Replace WebView with Native Compose UI for Messages
Labels: epic, enhancement, copilot-agent, high-priority

Description:
Replace the current WebView-based implementation with a native Jetpack Compose UI that provides better reliability, performance, and automotive compliance.

## Epic Goals
- Eliminate WebView QR code rendering issues
- Improve performance and memory usage
- Better automotive safety compliance
- Native Android integration capabilities

## Child Issues
- [ ] #2 Design and implement data models for messages
- [ ] #4 Implement background message synchronization service
- [ ] #3 Build native QR code pairing screen
- [ ] #5 Create conversation list UI in Compose
- [ ] #6 Implement message detail/reply UI
- [ ] Future: Add voice message recording/playbook
- [ ] Future: Integrate with notification system

## Success Criteria
- [ ] QR code pairing works reliably
- [ ] Message sync works in background
- [ ] UI follows automotive design guidelines
- [ ] Performance is better than WebView version
- [ ] All existing functionality preserved

@github-copilot This epic will be broken down into smaller issues for implementation.
```

### Individual Issues:

**Issue 1: Message Data Models**
```
Title: [FEATURE] Design and implement data models for messages and conversations
Labels: enhancement, copilot-agent, data-layer

## Feature Description
Create Kotlin data classes and Room entities to represent messages, conversations, and user data for local storage and API communication.

## Technical Requirements
- Use Room for local database
- Follow MVVM architecture
- Support message types: text, media, voice
- Include conversation metadata
- Support message status (sent, delivered, read)

## Acceptance Criteria
- [ ] Message data class with all required fields
- [ ] Conversation data class with metadata
- [ ] Room database entities and DAOs
- [ ] Database migration strategy
- [ ] Unit tests for data models

@github-copilot please implement this feature
```

**Issue 2: Background Sync Service**
```
Title: [FEATURE] Implement background message synchronization service
Labels: enhancement, copilot-agent, service, automotive

## Feature Description
Create a background service that synchronizes messages with Google Messages API while the app is running, especially important for automotive use cases.

## Technical Requirements
- Use WorkManager for reliable background processing
- Implement proper error handling and retry logic
- Respect automotive power management
- Handle network connectivity changes
- Implement message status updates

## Acceptance Criteria
- [ ] Background sync service implementation
- [ ] Proper lifecycle management
- [ ] Error handling and retry mechanisms
- [ ] Automotive power optimization
- [ ] Integration tests

@github-copilot please implement this feature
```

**Issue 3: Native QR Code Screen**
```
Title: [FEATURE] Build native QR code pairing screen using ZXing
Labels: enhancement, copilot-agent, ui, high-priority

## Feature Description
Replace the unreliable WebView QR code with a native Compose implementation using ZXing library for QR code generation and display.

## Technical Requirements
- Use ZXing library for QR code generation
- Implement in Jetpack Compose
- Follow automotive UI guidelines
- Large touch targets and clear visuals
- Handle pairing state management

## Acceptance Criteria
- [ ] Native QR code generation using ZXing
- [ ] Compose UI with automotive styling
- [ ] QR code always visible and properly sized
- [ ] Pairing state detection and updates
- [ ] Error handling for QR generation

@github-copilot please implement this feature
```

**Issue 4: Conversation List UI**
```
Title: [FEATURE] Create conversation list UI in Jetpack Compose
Labels: enhancement, copilot-agent, ui, automotive

## Feature Description
Build a native conversation list screen using Jetpack Compose that displays conversations with automotive-optimized UI elements.

## Technical Requirements
- Jetpack Compose implementation
- LazyColumn for performance
- Material 3 design with automotive customizations
- Large touch targets (minimum 48dp)
- Support for unread message indicators
- Proper accessibility support

## Acceptance Criteria
- [ ] Compose conversation list screen
- [ ] Automotive-compliant touch targets
- [ ] Unread message indicators
- [ ] Material 3 theming
- [ ] Accessibility support
- [ ] Performance optimized

@github-copilot please implement this feature
```
