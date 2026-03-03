# MessagesForCar - Copilot Instructions

<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

## Project Overview
MessagesForCar is an Android Automotive messaging application that uses a **hybrid architecture** combining a hidden WebView running Google Messages for Web with a native Jetpack Compose UI optimized for automotive use.

## Architecture Approach

### Hybrid Architecture Components
1. **Hidden WebView Layer**
   - Runs Google Messages for Web in the background
   - Handles authentication via QR code pairing
   - Executes JavaScript to extract message/conversation data from DOM
   - Receives action commands (send, mark read, react) via JavaScript injection

2. **Data Extraction & Storage**
   - JavaScript scrapes Messages Web DOM for conversations, messages, reactions, read status, etc.
   - Data passes through JavascriptInterface to Android layer
   - Room database stores extracted data (MessageEntity, ConversationEntity)
   - Repository pattern provides clean data access layer

3. **Native Compose UI**
   - Production UI is native Jetpack Compose (NOT WebView-based)
   - Reads data from Room database via repositories
   - Automotive-optimized layouts (landscape split-pane, portrait stacked)
   - Material 3 design with large touch targets
   - Mockup screens in `CarMessagesMockupScreen.kt` represent actual production UI

4. **Bidirectional Communication**
   - UI → WebView: User actions trigger JavaScript commands
   - WebView → UI: Data extraction updates Room database, triggers UI recomposition

### Key Benefits
- Legitimate use of Google's web interface (no API reverse engineering)
- Native automotive UI optimized for safety and usability
- Offline data access via local database
- Real-time sync when connected

## Key Features
- QR code pairing (restricted to parked state for safety)
- JavaScript-based data extraction from Google Messages Web
- Room database persistence with automotive-optimized queries
- Native Compose UI with desktop-like conversation/detail panes
- Voice messaging with speech recognition
- Driving state awareness and UI restrictions
- Android Automotive notifications with MessagingStyle

## Architecture Guidelines
- **WebView is hidden, used only for data/auth** - never shown to user in production
- **Compose UI is the production interface** - all user interaction happens in native UI
- Use Repository pattern to abstract data layer from UI
- Follow MVVM: ViewModels consume repositories, Composables observe ViewModels
- JavaScript extraction must be comprehensive (conversations, messages, reactions, read status, images, etc.)
- All WebView actions executed via `evaluateJavascript` with callbacks
- Follow Android Automotive best practices for distraction-free UX
- Implement proper permission handling for microphone and notifications

## Code Organization
- **`data/entity/`** - Room entities (MessageEntity, ConversationEntity, ContactEntity)
- **`data/dao/`** - Data access objects with automotive-optimized queries
- **`data/repository/`** - Repository pattern implementation
- **`ui/screens/`** - Native Compose screens (production UI)
- **`ui/components/`** - Reusable Compose components and WebView wrappers
- **`service/`** - Background services for WebView and sync operations
- **`sync/`** - WorkManager workers for data synchronization

## Code Style
- Follow Kotlin coding conventions
- Use Material 3 design system with automotive customizations
- Implement proper lifecycle management for activities and services
- Use Kotlin coroutines and Flow for async operations
- Repository methods return Flow for reactive data streams
- Use dependency injection where appropriate (Repository.getInstance pattern currently)

## Security Considerations
- Restrict WebView navigation to Google Messages domain only (`shouldOverrideUrlLoading`)
- WebView runs with minimal JavaScript permissions (only what's needed)
- Handle permissions gracefully with proper user feedback
- Exclude sensitive data from backups
- Implement proper authentication state management via PairingPreferences
- Never expose credentials or tokens

## Data Extraction Strategy
When implementing JavaScript extractors:
- Target specific DOM selectors for Google Messages Web
- Extract complete data models (not just text snippets)
- Handle both one-on-one and group conversations
- Capture message metadata (timestamps, read status, reactions, delivery status)
- Support rich content (images, links, voice messages)
- Implement efficient mutation observers for real-time updates
- Pass structured JSON through JavascriptInterface

## Testing Guidelines
- Test on Android Automotive OS emulator (17inch_Chevy_FF1_ARM)
- Verify driving state detection and UI restrictions
- Test pairing flow requires parked state
- Validate data extraction completeness
- Test Room database queries and performance
- Verify UI preview mode works without pairing (`ui_preview_mode` intent extra)
- Test voice recognition accuracy in various noise conditions
- Validate notification integration with automotive systems

## Documentation References
- See `HYBRID_ARCHITECTURE.md` for detailed architecture explanation
- See `DATA_MODEL_SUMMARY.md` for Room database schema details
- See `MESSAGE_PANE_FEATURES.md` for UI feature checklist
