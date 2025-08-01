#!/bin/bash

# GitHub Issues Creation Script for Messages-for-Car
# Run this script to create all the issues for the Native UI Refactor

REPO_OWNER="scerruti"
REPO_NAME="Messages-for-Car"

echo "🚀 Creating GitHub Issues for Native UI Refactor..."
echo "Repository: $REPO_OWNER/$REPO_NAME"
echo ""

# Check if GitHub CLI is installed
if ! command -v gh &> /dev/null; then
    echo "❌ GitHub CLI (gh) is not installed."
    echo "Please install it: https://cli.github.com/"
    echo "Then run: gh auth login"
    exit 1
fi

# Check if authenticated
if ! gh auth status &> /dev/null; then
    echo "❌ Not authenticated with GitHub CLI."
    echo "Please run: gh auth login"
    exit 1
fi

echo "✅ GitHub CLI is ready!"
echo ""

# Create Epic Issue
echo "📋 Creating Epic Issue..."
gh issue create \
    --title "[EPIC] Replace WebView with Native Compose UI for Messages" \
    --label "epic,enhancement,copilot-agent,high-priority" \
    --body "Replace the current WebView-based implementation with a native Jetpack Compose UI that provides better reliability, performance, and automotive compliance.

## Epic Goals
- Eliminate WebView QR code rendering issues
- Improve performance and memory usage
- Better automotive safety compliance
- Native Android integration capabilities

## Child Issues
- [ ] Design and implement data models for messages
- [ ] Create Room database for local message storage
- [ ] Implement background message synchronization service
- [ ] Build native QR code pairing screen
- [ ] Create conversation list UI in Compose
- [ ] Implement message detail/reply UI
- [ ] Add voice message recording/playback
- [ ] Integrate with notification system

## Success Criteria
- [ ] QR code pairing works reliably
- [ ] Message sync works in background
- [ ] UI follows automotive design guidelines
- [ ] Performance is better than WebView version
- [ ] All existing functionality preserved

@github-copilot This epic will be broken down into smaller issues for implementation."

echo "✅ Epic issue created!"
echo ""

# Create Issue 1: Message Data Models
echo "📋 Creating Issue 1: Message Data Models..."
gh issue create \
    --title "[FEATURE] Design and implement data models for messages and conversations" \
    --label "enhancement,copilot-agent,data-layer" \
    --body "## Feature Description
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

@github-copilot please implement this feature"

echo "✅ Issue 1 created!"
echo ""

# Create Issue 2: Background Sync Service
echo "📋 Creating Issue 2: Background Sync Service..."
gh issue create \
    --title "[FEATURE] Implement background message synchronization service" \
    --label "enhancement,copilot-agent,service,automotive" \
    --body "## Feature Description
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

@github-copilot please implement this feature"

echo "✅ Issue 2 created!"
echo ""

# Create Issue 3: Native QR Code Screen
echo "📋 Creating Issue 3: Native QR Code Screen..."
gh issue create \
    --title "[FEATURE] Build native QR code pairing screen using ZXing" \
    --label "enhancement,copilot-agent,ui,high-priority" \
    --body "## Feature Description
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

@github-copilot please implement this feature"

echo "✅ Issue 3 created!"
echo ""

# Create Issue 4: Conversation List UI
echo "📋 Creating Issue 4: Conversation List UI..."
gh issue create \
    --title "[FEATURE] Create conversation list UI in Jetpack Compose" \
    --label "enhancement,copilot-agent,ui,automotive" \
    --body "## Feature Description
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

@github-copilot please implement this feature"

echo "✅ Issue 4 created!"
echo ""

# Create Issue 5: Message Detail UI
echo "📋 Creating Issue 5: Message Detail UI..."
gh issue create \
    --title "[FEATURE] Implement message detail and reply UI in Compose" \
    --label "enhancement,copilot-agent,ui,automotive" \
    --body "## Feature Description
Build a message detail screen with reply functionality using Jetpack Compose, optimized for automotive use.

## Technical Requirements
- Jetpack Compose implementation
- Support text input with automotive considerations
- Voice message integration
- Automotive-safe input methods
- Material 3 design with large touch targets

## Acceptance Criteria
- [ ] Message detail screen in Compose
- [ ] Reply functionality with text input
- [ ] Voice message support
- [ ] Automotive input safety
- [ ] Material 3 theming
- [ ] Accessibility support

@github-copilot please implement this feature"

echo "✅ Issue 5 created!"
echo ""

echo "🎉 All issues created successfully!"
echo ""
echo "🔗 View your issues at: https://github.com/$REPO_OWNER/$REPO_NAME/issues"
echo ""
echo "Next steps:"
echo "1. Review the created issues"
echo "2. The Copilot agents should automatically start working on them"
echo "3. Monitor the progress in the GitHub repository"
