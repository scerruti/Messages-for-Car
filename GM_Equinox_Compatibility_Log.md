# GM Equinox Compatibility Log

This log tracks decisions, assumptions, and workarounds specific to the GM Equinox EV implementation of Android Automotive OS (AAOS). It helps future-proof the project for broader GM vehicle compatibility.

## Initial Assumptions (December 20, 2025)

### WebView Rendering on GM Head Units
**Decision/Assumption:** WebView will reliably render Google Messages for Web on GM Equinox head units, with JavaScript injection for pairing detection and UI optimizations working as expected.  
**Why Equinox-Specific:** GM's AAOS implementation may have custom WebView configurations or restrictions on head units that differ from standard Android Automotive or other OEMs. Equinox uses a specific head unit hardware/software stack that could affect WebView performance, security policies, or JavaScript execution.  
**Differences on Other GM Vehicles:** Other GM vehicles (e.g., Chevy Bolt EV, Cadillac models) may have different head unit firmware versions or AAOS customizations that could break WebView-based messaging or require different JavaScript workarounds.  
**Revisit Later:** Test on additional GM vehicles when expanding compatibility; monitor for GM AAOS updates that change WebView behavior; consider native API fallbacks if WebView proves unreliable.

### Driving-Mode UI Restrictions
**Decision/Assumption:** The app will enforce distraction-minimized UI (large touch targets, no keyboard, minimal scrolling) only when the vehicle is in motion, using standard AAOS driving state detection.  
**Why Equinox-Specific:** GM's AAOS may have proprietary driving state APIs or head unit behaviors that restrict app interactions differently than standard AAOS (e.g., more aggressive UI lockdowns or custom safety overlays).  
**Differences on Other GM Vehicles:** Driving restrictions might vary by vehicle model or head unit generation; some may allow more UI flexibility while others are stricter.  
**Revisit Later:** Validate driving state detection accuracy across GM models; test UI restrictions in real driving scenarios; check for GM-specific safety API extensions.

### Media Session Behavior During Voice Input
**Decision/Assumption:** AudioManager/MediaSession will pause and resume media playback reliably during SpeechRecognizer sessions on Equinox.  
**Why Equinox-Specific:** GM's AAOS may integrate media sessions with vehicle audio systems in proprietary ways, potentially affecting pause/resume behavior or conflicting with navigation audio.  
**Differences on Other GM Vehicles:** Media handling could differ based on infotainment system versions or vehicle audio architectures.  
**Revisit Later:** Test media pause/resume in Equinox with various media apps (e.g., Spotify, Android Auto); compare behavior on other GM vehicles; monitor for AAOS updates affecting audio focus.

## Implementation Status (Current - MVP Complete)

### Core Features Implemented
**WebView Integration:** Successfully integrated Google Messages for Web with JavaScript injection for pairing detection and text input. Tested on standard Android; assumes compatibility with GM Equinox head units.  
**Voice Recognition:** Implemented SpeechRecognizer with automotive-optimized UI (large button, no keyboard). Pauses/resumes media during voice input using MediaController.  
**UI/UX:** Driving-safe interface with large touch targets, status overlays, and conditional pairing prompts. Uses Material 3 with automotive customizations.  
**Background Sync:** WorkManager-based message synchronization with automotive power management.  
**Notifications:** MessagesBackgroundService provides foreground WebView monitoring and notification creation for new messages.  
**Pairing:** QR code pairing with WebView state detection; restricts pairing to parked state.  

### GM Equinox Specific Considerations
**Head Unit Compatibility:** WebView rendering and JS injection assumed functional; may need adjustments for Equinox-specific WebView policies or hardware acceleration.  
**Audio Integration:** Media pause/resume implemented via standard MediaController; may interact differently with GM's proprietary audio system.  
**Driving State:** UI restrictions enforced via AutomotiveHelper; validate Equinox driving detection APIs.  
**Performance:** WebView memory usage and rendering performance not yet tested on Equinox hardware.  

**Next Steps:** Deploy to Equinox for real-world testing; monitor WebView stability, audio conflicts, and driving state accuracy; update log with findings.</content>
<parameter name="filePath">/Users/scerruti/Messages for Car/GM_Equinox_Compatibility_Log.md