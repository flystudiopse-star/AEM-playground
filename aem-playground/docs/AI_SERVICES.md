# AEM Playground - AI Services Documentation

## 1. Core AI Services

### OpenAIService
**Purpose:** Main AI text and image generation service with OpenAI API integration and caching.

**Configuration (OSGi):**
- `apiKey` - OpenAI API key
- `textEndpoint` - Text generation endpoint (default: https://api.openai.com/v1/chat/completions)
- `imageEndpoint` - Image generation endpoint (default: https://api.openai.com/v1/images/generations)
- `defaultModel` - Default text model (default: gpt-4)
- `defaultImageModel` - Default image model (default: dall-e-3)
- `cacheMaxSize` - Maximum cache entries
- `cachingEnabled` - Enable/disable caching

**Key Methods:**
| Method | Description |
|--------|-------------|
| `generateText(String prompt, AIGenerationOptions options)` | Generate text using OpenAI GPT-4 |
| `generateImage(String prompt, AIGenerationOptions options)` | Generate images using DALL-E |
| `clearCache()` | Clear all cached results |

---

### AIService Interface
**Purpose:** Base interface for AI services.

**Methods:**
- `generateText(String prompt, AIGenerationOptions options)` - Text generation
- `generateImage(String prompt, AIGenerationOptions options)` - Image generation  
- `clearCache()` - Clear cache

---

## 2. Content Services

### SEOOptimizerService
**Purpose:** AI-powered SEO optimization and metadata generation.

**Key Methods:**
| Method | Description |
|--------|-------------|
| `generateMetadata(pageContent, pageTitle, pagePath)` | Generate SEO metadata |
| `generateMetadata(..., language)` | Generate with language |
| `generateSitemapXml(entries)` | Generate XML sitemap |
| `calculateSeoScore(metadata)` | Calculate SEO score |
| `generateSchemaOrgJsonLd(...)` | Generate Schema.org JSON-LD |
| `generateOpenGraphMetadata(...)` | Generate OpenGraph tags |
| `generateTwitterCardMetadata(...)` | Generate Twitter Card tags |

---

### AISmartSearchService
**Purpose:** AI-powered search with semantic understanding and indexing.

**Key Methods:**
| Method | Description |
|--------|-------------|
| `search(query, options)` | Execute semantic search |
| `indexContent(contentId, content, type)` | Index single content |
| `indexContentBatch(contents)` | Batch index content |
| `removeFromIndex(contentId)` | Remove from index |
| `getSuggestions(partialQuery, max)` | Auto-complete suggestions |
| `rebuildIndex()` | Rebuild search index |

**Inner Classes:**
- `SearchResult` - Search results with hits, suggestions, timing
- `SearchHit` - Individual search result with score and highlights
- `SearchOptions` - Search configuration (maxResults, minScore, etc.)
- `ContentToIndex` - Content to be indexed

---

### ContentForecastService
**Purpose:** AI-powered content performance prediction and analytics.

**Key Methods:**
| Method | Description |
|--------|-------------|
| `predictContentPerformance(path, type)` | Predict content performance |
| `forecastTraffic(path, type, days)` | Forecast traffic |
| `suggestPublishSchedule(path, type)` | Suggest optimal publish time |
| `identifyTrendingTopics(paths)` | Identify trending topics |
| `generateContentCalendar(start, end, types)` | Generate content calendar |
| `generateAnalyticsDashboard()` | Generate analytics dashboard |

---

### ContentQACheckerService
**Purpose:** AI-powered content quality assurance and validation.

**Key Methods:**
| Method | Description |
|--------|-------------|
| `analyzeContent(content, path)` | Full content analysis |
| `checkBrokenLinks(content, path)` | Detect broken links |
| `validateStructure(content, path)` | Validate HTML structure |
| `checkAccessibility(content, path)` | Check WCAG accessibility |
| `checkBrandConsistency(content, path)` | Check brand guidelines |
| `generateFullReport(content, path)` | Generate comprehensive QA report |

---

### AIContentTransformerService
**Purpose:** Transform content between different formats (HTML, Markdown, AEM HTL).

**Key Methods:**
| Method | Description |
|--------|-------------|
| `transformContent(content, options)` | Transform content format |
| `generateMetadata(content, title)` | AI-generated metadata |
| `cleanupAndOptimize(content, options)` | Clean and optimize HTML |
| `generateImageAltText(imageData, name)` | Generate alt text for images |
| `suggestComponentMappings(html)` | Suggest AEM component mappings |
| `generateRedirectMappings(sourceUrls, target)` | Generate redirect mappings |

---

## 3. Component & Template Services

### ComponentBuilderService
**Purpose:** AI-powered AEM component generation.

**Key Methods:**
| Method | Description |
|--------|-------------|
| `buildComponent(name, description, responsive, crud)` | Build complete component |
| `generateFields(description)` | Generate component fields |
| `generateDialogXml(fields)` | Generate dialog XML |
| `generateContentXml(name, title, group)` | Generate content XML |
| `generateHtTemplate(name, fields, responsive)` | Generate HTL template |
| `generateSlingModel(name, fields, crud)` | Generate Sling Model |
| `generateCss(name, responsive)` | Generate CSS |

---

### TemplateGenerator / TemplateGeneratorImpl
**Purpose:** AI-powered page template generation.

**Key Methods:**
| Method | Description |
|--------|-------------|
| `generateTemplate(templateName, description)` | Generate page template |
| `generateTemplateWithComponents(templateName, components)` | Generate with components |
| `getAvailableTemplates()` | List available templates |

---

## 4. Migration Services

### SharePointMigrationService
**Purpose:** Migrate content from SharePoint to AEM with AI transformation.

**Configuration (OSGi):**
- `sharepoint_site_url` - SharePoint site URL
- `sharepoint_client_id` - API client ID
- `sharepoint_client_secret` - API client secret
- `sharepoint_tenant_id` - Azure AD tenant ID
- `sharepoint_site_name` - Site name
- `api_timeout` - API timeout (default: 30000ms)
- `enable_ai_transformation` - AI content transformation
- `enable_ai_metadata` - AI metadata generation
- `enable_ai_image_alt` - AI image alt text
- `enable_ai_cleanup` - AI content cleanup

**Key Methods:**
| Method | Description |
|--------|-------------|
| `fetchPages(maxPages)` | Fetch pages from SharePoint |
| `getPageContent(pageId)` | Get page content |
| `fetchAssets(maxAssets)` | Fetch assets from SharePoint |
| `downloadAsset(fileRef)` | Download asset binary |
| `transformContentWithAi(content)` | Transform content with AI |
| `generateImageAltText(data, name)` | Generate alt text |
| `suggestComponentMappings(html)` | Suggest AEM mappings |
| `generateRedirectMappings(pages, target)` | Generate redirects |

---

## 5. Key DTOs

| DTO | Purpose |
|-----|---------|
| `SEOMetadata` | SEO metadata (title, description, keywords, etc.) |
| `SitemapEntry` | Sitemap URL entry |
| `ContentQAReport` | Quality assurance report |
| `PerformancePrediction` | Content performance prediction |
| `TrafficForecast` | Traffic forecast data |
| `TrendingTopic` | Trending topic information |
| `ContentCalendar` | Content calendar with publish dates |
| `AnalyticsDashboard` | Analytics dashboard data |
| `ComponentDescriptor` | AEM component descriptor |
| `ABTestSuggestion` | A/B test suggestions |

---

## 6. Pending Services (Awaiting Merge)

The following AI services were implemented but are pending merge due to authentication issues:

1. **TranslationService** - AI content translation (8 languages)
2. **PersonalizationService** - User segment-based personalization
3. **WorkflowAutomationService** - AI workflow optimization
4. **SentimentAnalyzer** - Content sentiment analysis
5. **EngagementScorer** - User engagement scoring
6. **AutoTaggingService** - AI-powered auto-tagging
7. **SummarizationService** - Content summarization
8. **ContentSchedulerService** - Optimal publish time scheduling
9. **ContentRecommenderService** - Personalized content recommendations
10. **ContentModerationService** - Content moderation/filtering
11. **DAMMetadataManagerService** - DAM metadata management
12. **ErrorDetectionService** - AI error detection in content

---

## Usage Examples

### OpenAIService
```java
@Reference
private AIService aiService;

public void generateContent() {
    AIGenerationOptions options = AIGenerationOptions.builder()
        .model("gpt-4")
        .temperature(0.7)
        .maxTokens(1000)
        .build();
    
    AIGenerationResult result = aiService.generateText(
        "Write a product description for AEM components", 
        options
    );
    
    if (result.isSuccess()) {
        String content = result.getContent();
        // Use generated content
    }
}
```

### SEOOptimizerService
```java
@Reference
private SEOOptimizerService seoService;

public void optimizeContent(String content, String title, String path) {
    SEOMetadata metadata = seoService.generateMetadata(content, title, path);
    String sitemap = seoService.generateSitemapXml(entries);
    String schema = seoService.generateSchemaOrgJsonLd(title, description, url, "Article");
}
```

### SharePointMigrationService
```java
@Reference
private SharePointMigrationService migrationService;

public void migrateContent() throws IOException {
    List<SharePointPage> pages = migrationService.fetchPages(50);
    for (SharePointPage page : pages) {
        SharePointPageContent content = migrationService.getPageContent(page.getId());
        TransformationResult result = migrationService.transformContentWithAi(content);
        // Save transformed content to AEM
    }
}
```