# GitHub Repository Setup Instructions

## 1. Create GitHub Repository

1. Go to [GitHub.com](https://github.com) and sign in
2. Click the "+" icon in the top right and select "New repository"
3. Repository settings:
   - **Repository name**: `messages-for-car`
   - **Description**: `Android Automotive messaging application that wraps Google Messages for Web with automotive-specific features`
   - **Visibility**: Choose Public or Private
   - **DO NOT** initialize with README, .gitignore, or license (we already have these)

## 2. Add Remote and Push

After creating the repository, run these commands in your terminal:

```bash
# Add the GitHub repository as remote origin
git remote add origin https://github.com/YOUR_USERNAME/messages-for-car.git

# Push the code to GitHub
git branch -M main
git push -u origin main
```

## 3. Set Up GitHub Issues and Projects

### Enable GitHub Issues:
1. Go to your repository on GitHub
2. Click "Settings" tab
3. Scroll to "Features" section
4. Ensure "Issues" is checked

### Create GitHub Project:
1. Go to your repository
2. Click "Projects" tab
3. Click "New project"
4. Choose "Board" template
5. Name it "MessagesForCar Development"
6. Add columns: Backlog, To Do, In Progress, Review, Done

### Import Tasks from Kanban Board:
You can manually create issues from the tasks in `.vscode/kanban.md` or use the GitHub CLI to automate this.

## 4. GitHub Extensions Integration

The following VS Code extensions are now installed and configured:
- **GitHub Pull Requests and Issues**: View and manage issues/PRs directly in VS Code
- **Kanban Board**: Local project management (`.vscode/kanban.md`)
- **Project Manager**: Easy switching between projects

## 5. Recommended GitHub Workflow

1. **Issues**: Create GitHub issues for bugs and features
2. **Branches**: Create feature branches for each issue
3. **Pull Requests**: Use PRs for code review before merging
4. **Projects**: Track progress using GitHub Projects board
5. **Actions**: Set up CI/CD for automated testing and builds

## 6. Next Steps

1. Create the GitHub repository using the instructions above
2. Set up GitHub Projects board
3. Create initial issues from the kanban board
4. Consider setting up GitHub Actions for automated APK builds
5. Enable branch protection rules for main branch

## 7. Local Development Workflow

```bash
# Start new feature
git checkout -b feature/native-qr-ui
git add .
git commit -m "feat: implement native QR code UI"
git push origin feature/native-qr-ui

# Create PR on GitHub, get review, merge
git checkout main
git pull origin main
```

This setup provides a complete Agile workflow with issue tracking, project management, and version control!
