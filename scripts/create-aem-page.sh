#!/bin/bash
# AEM Page Creator Script
#
# Usage: ./create-aem-page.sh "Page Title" [parent-path]
# Example: ./create-aem-page.sh "My New Page"
# Example: ./create-aem-page.sh "Bank Form"
#
# NOTE: AEM may have caching issues - if .html returns 404, try:
#   1. Clear browser cache (Ctrl+Shift+R)
#   2. Clear AEM cache via CRX DE: /crx/de/
#   3. Check page exists via: /content/.../.json

set -e

AEM_HOST="http://192.168.1.176:4502"
AEM_USER="admin"
AEM_PASS="admin"

PAGE_TITLE="${1:-New Page}"
PARENT_PATH="${2:-/content/aem-playground/us/en}"
PAGE_NAME=$(echo "$PAGE_TITLE" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9]*//g')
PAGE_PATH="${PARENT_PATH}/${PAGE_NAME}"

echo "AEM Page Creator"
echo "Title: $PAGE_TITLE"
echo "Path:  $PAGE_PATH"

# Create page
echo "Creating page..."
curl -s -u "$AEM_USER:$AEM_PASS" -X POST "$AEM_HOST${PARENT_PATH}" \
    -F "jcr:primaryType=cq:Page" \
    -o /dev/null

# Set content
echo "Setting content..."
curl -s -u "$AEM_USER:$AEM_PASS" -X POST "$AEM_HOST${PAGE_PATH}" \
    -F "jcr:content/jcr:primaryType=cq:PageContent" \
    -F "jcr:content/sling:resourceType=aem-playground/components/page" \
    -F "jcr:content/title=$PAGE_TITLE" \
    -o /dev/null

# Add text
curl -s -u "$AEM_USER:$AEM_PASS" -X POST "$AEM_HOST${PAGE_PATH}/jcr:content" \
    -F "_=text" \
    -F "jcr:primaryType=nt:unstructured" \
    -F "sling:resourceType=foundation/components/text" \
    -F "text=$PAGE_TITLE" \
    -o /dev/null

echo ""
echo "Done!"
echo "View: $AEM_HOST${PAGE_PATH}.html"
