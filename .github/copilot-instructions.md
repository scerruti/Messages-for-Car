# MessagesForCar - Copilot Instructions

<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

## Project Overview
MessagesForCar is an Android Automotive messaging application that wraps Google Messages for Web with automotive-specific features.

## Key Features
- WebView integration with Google Messages for Web
- Voice messaging with speech recognition
- Android Automotive Car App integration
- Driving state awareness (QR pairing only when parked)
- Automotive-optimized UI with larger touch targets

## Architecture Guidelines
- Use Jetpack Compose for modern UI development
- Implement WebView with security considerations (domain restrictions)
- Follow Android Automotive best practices for distraction-free UX
- Use Car App Library for automotive-specific screens
- Implement proper permission handling for microphone access

## Code Style
- Follow Kotlin coding conventions
- Use Material 3 design system with automotive customizations
- Implement proper lifecycle management for activities and services
- Use dependency injection where appropriate
- Follow MVVM architecture pattern

## Security Considerations
- Restrict WebView navigation to Google Messages domain only
- Handle permissions gracefully with proper user feedback
- Exclude sensitive data from backups
- Implement proper authentication state management

## Testing Guidelines
- Test on actual automotive hardware when possible
- Verify voice recognition accuracy in various noise conditions
- Test driving state detection and UI restrictions
- Validate WebView performance and memory usage
