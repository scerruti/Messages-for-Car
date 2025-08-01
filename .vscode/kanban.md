# MessagesForCar Project Kanban Board

## Backlog
- [ ] **Epic: Native UI Refactor** - Replace WebView with native Compose UI
  - [ ] Research native message sync architecture
  - [ ] Design message data models and Room database
  - [ ] Implement background message sync service
  - [ ] Create native QR code pairing screen
  - [ ] Build conversation list UI in Compose
  - [ ] Implement message detail/reply UI
  - [ ] Add voice message recording/playback
  - [ ] Handle notification integration

- [ ] **Epic: Automotive Integration** - Full Car App Library implementation
  - [ ] Enable and test Car App screens
  - [ ] Implement driving state detection
  - [ ] Add automotive-specific navigation
  - [ ] Test distraction guidelines compliance

- [ ] **Epic: Performance & Testing** - Production readiness
  - [ ] Add unit tests for core components
  - [ ] Performance testing on automotive hardware
  - [ ] Memory usage optimization
  - [ ] Battery usage optimization

## To Do
- [ ] Create GitHub issues for native UI refactor (use templates)
- [ ] Set up GitHub Projects board for sprint management
- [ ] Define architectural decision records (ADRs)
- [ ] Test Copilot agent workflows

## Ready for Copilot Agent
- [ ] Fix QR code visibility (short-term WebView fix)
- [ ] Create native QR code pairing screen
- [ ] Implement message data models and Room database
- [ ] Build background message sync service
- [ ] Create conversation list UI in Compose

## In Progress
- [x] Set up Git repository
- [x] Configure VS Code workspace
- [x] Install Agile extensions
- [x] Create GitHub repository with workflows
- [x] Set up Copilot agent automation

## Done
- [x] Initial project setup with Compose and Car App Library
- [x] WebView integration with Google Messages
- [x] Basic automotive UI optimizations
- [x] Voice activity implementation
- [x] QR code detection logic (needs fixing)
- [x] Project structure and build configuration

## Notes
- Current WebView approach has QR code rendering issues
- Planning native UI refactor for better reliability
- Need to establish GitHub repository for collaboration
- Consider implementing progressive enhancement: WebView + fallback native UI
