# Messages for Car - Hybrid Architecture

## Overview

MessagesForCar implements a **hybrid architecture** that combines:

1. **Background WebView Service** - Maintains connection to Google Messages for Web
2. **Android Automotive Notifications** - Creates proper messaging notifications 
3. **Voice Integration** - Handles reply/read actions through automotive systems

## Architecture Components

### 1. MessagesBackgroundService
- Runs a hidden WebView connected to Google Messages for Web
- Intercepts new messages via JavaScript injection
- Creates Android Automotive notifications with proper `MessagingStyle`
- Handles reply and mark-as-read actions by executing JavaScript

### 2. MessageActionService
- IntentService that processes notification actions
- Handles reply actions with `RemoteInput` for voice/text input
- Handles mark-as-read actions
- Communicates back to the WebView service to execute actions

### 3. MainActivity
- Main UI showing the visible WebView (optional)
- Starts and manages the background service
- Shows service status and automotive information
- Handles permissions (notifications, microphone)

### 4. AutomotiveHelper
- Utility class for automotive-specific functionality
- Detects Android Automotive OS
- Provides automotive-optimized settings

## Key Features

### ✅ Background Operation
- WebView operates without being displayed
- Maintains persistent connection to Google Messages
- Continues running when main UI is closed

### ✅ Android Automotive Integration
- Creates `NotificationCompat.MessagingStyle` notifications
- Appears in car's native notification system
- Supports voice replies through automotive UI
- Proper heads-up notifications while driving

### ✅ Message Interception
- JavaScript monitors DOM changes for new messages
- Extracts sender, content, and timestamp
- Real-time message detection

### ✅ Two-Way Communication
- Reply to messages through voice or text input
- Mark conversations as read
- Actions executed via JavaScript in the background WebView

## How It Works

1. **Service Startup**: Background service starts and loads Google Messages for Web in hidden WebView
2. **JavaScript Injection**: Custom JavaScript monitors for new messages
3. **Message Detection**: When new message arrives, JavaScript extracts data and calls Android interface
4. **Notification Creation**: Service creates Android Automotive notification with reply/read actions
5. **User Interaction**: User replies via voice in car, or marks as read
6. **Action Execution**: Action service receives intent and sends commands back to WebView
7. **WebView Execution**: Background WebView executes JavaScript to send reply or mark as read

## Benefits

- **✅ Legitimate**: Uses Google's official web interface
- **✅ Real-time**: Live connection with immediate notifications
- **✅ Automotive Native**: Proper integration with car notification systems
- **✅ Voice Enabled**: Full voice reply support
- **✅ Background**: Works without visible UI
- **✅ Reliable**: No API dependencies or reverse engineering

## Permissions Required

```xml
<!-- Core functionality -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Automotive integration -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Voice input -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## Testing

### On Android Automotive OS Emulator:
1. Install and run the app
2. Grant notification permissions
3. Background service starts automatically
4. Send a test message to your Google Messages account
5. Check for automotive notification with reply/read options

### On Regular Android (for development):
1. Same steps as above
2. Notifications appear as regular Android notifications
3. Voice reply may not work (depends on device capabilities)

## Future Enhancements

- **Contact Photos**: Extract contact images from Google Messages
- **Group Chat Support**: Enhanced group conversation handling
- **Smart Replies**: AI-generated quick reply suggestions
- **Driving Mode**: UI adaptations based on driving state
- **Car Integration**: Integration with car controls and displays
- **Offline Caching**: Cache recent messages for offline viewing

## Troubleshooting

### Service Not Starting
- Check notification permissions
- Verify internet connection
- Check logcat for WebView errors

### Messages Not Intercepted
- Ensure Google Messages for Web is working in regular browser
- Check JavaScript console logs
- Verify DOM selectors match current Google Messages structure

### Notifications Not Appearing
- Check notification permissions
- Verify notification channels are created
- Check automotive feature detection

### Voice Replies Not Working
- Verify microphone permissions
- Check automotive/car app integration
- Test with regular text input first

## Technical Notes

- **JavaScript Selectors**: May need updates if Google Messages changes their DOM structure
- **WebView Security**: Uses domain restrictions and secure settings
- **Memory Management**: Hidden WebView is properly cleaned up on service destruction
- **Network Handling**: Includes offline/connection error handling
- **Threading**: All WebView operations run on main thread as required

This hybrid architecture provides the reliability of WebView with the native integration required for automotive use cases.
