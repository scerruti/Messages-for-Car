# Message Pane Features Checklist

## Visual Elements

### Date & Time
- [ ] Day dividers (centered "Monday · 2:40 PM", "Today", etc.)
- [ ] Message timestamps (shown on individual messages or groups)
- [ ] Smart timestamp grouping (hide redundant timestamps for consecutive messages)

### Message Attribution
- [ ] Sender names (shown above messages in group conversations)
- [ ] Profile avatars (for group conversations)
- [ ] Avatar grouping (consecutive messages from same sender)

### Message Status
- [ ] Read receipts
  - [ ] One-on-one: "Read by [name]" with timestamp
  - [ ] Group: Multiple read indicators, expandable to see who read
- [ ] Delivery status indicators (checkmarks)
  - [ ] Single check: sent
  - [ ] Double check: delivered
  - [ ] Blue checks: read (if enabled)
- [ ] Encryption status indicator (lock icon for RCS/encrypted messages)
- [ ] Send failure indicators

### Rich Content
- [ ] Images
  - [ ] Inline image display
  - [ ] Multiple images in grid layout
  - [ ] Image loading states
  - [ ] Tap to open full-screen viewer
- [ ] Links
  - [ ] Blue clickable links
  - [ ] Rich link previews (title, description, thumbnail)
  - [ ] URL display in blue chip
- [ ] Reactions (emoji reactions on messages)
  - [ ] Display reactions below/beside message bubble
  - [ ] Show reaction count
  - [ ] Tap to see who reacted (group chats)
  - [ ] Add reaction UI

## Interactive Features

### Message Interactions
- [ ] Long-press message menu
  - [ ] Copy text
  - [ ] Forward
  - [ ] Delete
  - [ ] Add reaction
  - [ ] View details
- [ ] Swipe to reply (quote previous message)
- [ ] Tap image to expand
- [ ] Tap link to open
- [ ] Tap reaction to add yours / see who reacted

### Real-time Updates
- [ ] Typing indicators (three dots when someone is composing)
- [ ] New message animations
- [ ] Read status updates
- [ ] Delivery status updates

## Content Types

### Implemented
- [x] Text messages
- [x] Basic timestamps

### To Implement
- [ ] Voice messages (audio playback UI)
- [ ] Stickers
- [ ] GIFs
- [ ] Location pins
- [ ] Contact cards
- [ ] Files/documents
- [ ] Message effects (emphasis styling)

## Message Composition

### Current
- [x] Text input field
- [x] Send button
- [x] "RCS message" placeholder

### To Add
- [ ] Attachment button (gallery, camera, files)
- [ ] Voice recording button
- [ ] Emoji picker
- [ ] Sticker/GIF picker
- [ ] Draft saving
- [ ] Character counter (SMS mode)
- [ ] Send button state (enabled only when text present)

## Automotive-Specific Considerations

### Priority Features for Car Use
- [ ] Large touch targets for all interactive elements
- [ ] Read receipts (important for async communication)
- [ ] Reactions (quick responses while parked)
- [ ] Images (view shared photos/screenshots)
- [ ] Voice messages (natural for car context)

### Lower Priority for Car Use
- [ ] Stickers/GIFs (less critical in automotive context)
- [ ] Complex message effects
- [ ] Extensive formatting options

## Notes
- All interactive features should only be available while parked
- Consider read-only message viewing while driving (if allowed by regulations)
- Voice messages are particularly relevant for automotive use cases
- Large images may need data usage warnings on cellular
