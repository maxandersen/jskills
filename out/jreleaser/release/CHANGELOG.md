## Changelog

## 🚀 Features
- 23f5f91 early-access pre-release on every push to main
- 8c68fe7 add JReleaser release flow matching upstream versioning
- 03d9f0b replace all Scanner prompts with TamboUI interactive prompts
- b462814 TamboUI-based interactive prompts for add command
- 604331d interactive fzf-style find using TamboUI

## 🐛 Fixes
- 9a203b0 use GITHUB_TOKEN for JReleaser (no custom secret needed)
- 6cf168c blob download hangs on large repos when --skill filter not passed
- 4535d8f don't block redirects to openclaw, only explicit openclaw sources
- 7bd93ab blob download fails on renamed/redirected GitHub repos
- 36277a0 detect openclaw redirects before cloning
- 467d6da clone hanging silently + SLF4J warnings
- 575cbb6 isSubpathSafe fails on macOS due to /var symlink to /private/var
- 262cb4a NPE when virtual search thread outlives TamboUI runner
- 13364ca align interactive find with upstream behavior

## 🧰 Tasks
- 3e03cb5 remove accidentally committed files


## Contributors
We'd like to thank the following people for their contributions:
Max Rydahl Andersen