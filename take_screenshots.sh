#!/bin/bash
# Helper script to capture plugin screenshots for README
# Run this from Terminal.app (which has screen capture permissions)

SCREENSHOTS_DIR="$(cd "$(dirname "$0")" && pwd)/screenshots"
mkdir -p "$SCREENSHOTS_DIR"

echo "=== Jaspr Plugin Screenshot Capture ==="
echo "This script will guide you through capturing screenshots."
echo "Screenshots will be saved to: $SCREENSHOTS_DIR"
echo ""

capture() {
    local name="$1"
    local description="$2"
    echo "--- Screenshot: $name ---"
    echo "  $description"
    echo "  Press Enter when ready, then select the area to capture..."
    read -r
    screencapture -i "$SCREENSHOTS_DIR/$name.png"
    if [ -f "$SCREENSHOTS_DIR/$name.png" ]; then
        echo "  Saved: $SCREENSHOTS_DIR/$name.png"
    else
        echo "  Skipped."
    fi
    echo ""
}

capture "new_project_wizard" \
    "Open IntelliJ > New Project > select 'Jaspr' in the left panel. Show the wizard form."

capture "code_vision" \
    "Open a Jaspr project with components. Show scope labels (Server/Client/Island) above classes."

capture "run_configurations" \
    "Show the run configuration dropdown with 'jaspr serve' and 'jaspr build' options."

capture "code_completion" \
    "Open a .dart file in a Jaspr project and trigger code completion for Jaspr helpers."

capture "component_highlighting" \
    "Show a file with @client or @island annotated components with colored highlighting."

echo "=== Done! ==="
echo "Screenshots saved to $SCREENSHOTS_DIR/"
echo "Now commit and push:"
echo "  git add screenshots/ && git commit -m 'Add plugin screenshots' && git push"
