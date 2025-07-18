# Z.ai Open Platform Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/ai.z.openapi/zai-sdk.svg)](https://search.maven.org/artifact/ai.z.openapi/zai-sdk)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-1.8%2B-orange.svg)](https://www.oracle.com/java/)

[English Readme](README.md)

Z.ai AI 平台官方 Java SDK，提供统一接口访问强大的AI能力，包括对话补全、向量嵌入、图像生成、音频处理等功能。

## ✨ 特性

- 🚀 **类型安全**: 所有接口完全类型封装，无需查阅API文档即可完成接入
- 🔧 **简单易用**: 简洁直观的API设计，快速上手
- ⚡ **高性能**: 基于现代Java库构建，性能优异
- 🛡️ **安全可靠**: 内置身份验证和令牌管理
- 📦 **轻量级**: 最小化依赖，易于项目集成

## 📦 安装

### 环境要求
- Java 1.8 或更高版本
- Maven 或 Gradle
- 尚不支持在 Android 平台运行

### Maven 依赖
在您的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>ai.z</groupId>
    <artifactId>zai-sdk</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Gradle 依赖
在您的 `build.gradle` 中添加以下依赖（适用于 Groovy DSL）：

```groovy
dependencies {
    implementation 'ai.z:zai-sdk:0.0.1'
}
```

或 `build.gradle.kts`（适用于 Kotlin DSL）：

```kotlin
dependencies {
    implementation("ai.z:zai-sdk:0.0.1")
}
```

### 📋 核心依赖

本SDK使用以下核心依赖库：

| 依赖库 | 版本 |
|--------|------|
| OkHttp | 4.12.0 |
| Java JWT | 4.4.0 |
| Jackson | 2.17.2 |
| Retrofit2 | 2.11.0 |
| RxJava | 3.1.8 |
| SLF4J | 2.0.16 |

## 🚀 快速开始

### 基本用法

1. **使用API凭证创建ZaiClient**
2. **通过客户端访问服务**
3. **使用类型化参数调用API方法**

```java
import ai.z.openapi.ZaiClient;
import ai.z.openapi.service.model.*;
import ai.z.openapi.core.Constants;

// 创建客户端 推荐使用环境变量设置API凭证
// export ZAI_API_KEY=your.api.key
ZaiClient client = ZaiClient.builder().build();

// 或代码设置凭证
ZaiClient client = ZaiClient.builder()
    .apiKey("your.api.key.your.api.secret")
    .build();

// 或为特定平台创建客户端
ZaiClient zhipuClient = ZaiClient.ofZHIPU("your.api.key.your.api.secret").build();
```

### 客户端配置

SDK提供了灵活的构建器模式来自定义您的客户端：

```java
ZaiClient client = ZaiClient.builder()
    .apiKey("your.api.key.your.api.secret")
    .baseUrl("https://api.z.ai/api/paas/v4/")
    .enableTokenCache()
    .tokenExpire(3600000) // 1小时
    .connectionPool(10, 5, TimeUnit.MINUTES)
    .build();
```

## 💡 使用示例

### 对话补全

```java
import ai.z.openapi.ZaiClient;
import ai.z.openapi.service.model.*;
import ai.z.openapi.core.Constants;
import java.util.Arrays;

// 创建客户端
ZaiClient client = ZaiClient.builder()
    .apiKey("your.api.key.your.api.secret")
    .build();

// 创建对话请求
ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
    .model(Constants.ModelChatGLM4)
    .messages(Arrays.asList(
        ChatMessage.builder()
            .role(ChatMessageRole.USER.value())
            .content("你好，你怎么样？")
            .build()
    ))
    .stream(false)
    .temperature(0.7f)
    .maxTokens(1024)
    .build();

// 执行请求
ChatCompletionResponse response = client.chat().createChatCompletion(request);

if (response.isSuccess()) {
    String content = response.getData().getChoices().get(0).getMessage().getContent();
    System.out.println("回复: " + content);
} else {
    System.err.println("错误: " + response.getMsg());
}
```

### 流式对话

```java
// 创建流式请求
ChatCompletionCreateParams streamRequest = ChatCompletionCreateParams.builder()
    .model(Constants.ModelChatGLM4)
    .messages(Arrays.asList(
        ChatMessage.builder()
            .role(ChatMessageRole.USER.value())
            .content("给我讲个故事")
            .build()
    ))
    .stream(true) // 启用流式
    .build();

// 执行流式请求
ChatCompletionResponse response = client.chat().createChatCompletion(streamRequest);

if (response.isSuccess() && response.getFlowable() != null) {
    response.getFlowable().subscribe(
        data -> {
            // 处理流式数据块
            if (data.getChoices() != null && !data.getChoices().isEmpty()) {
                String content = data.getChoices().get(0).getDelta().getContent();
                if (content != null) {
                    System.out.print(content);
                }
            }
        },
        error -> System.err.println("\n流式错误: " + error.getMessage()),
        () -> System.out.println("\n流式完成")
    );
}
```

### 函数调用

```java
// 定义函数
ChatTool weatherTool = ChatTool.builder()
    .type(ChatToolType.FUNCTION.value())
    .function(ChatFunction.builder()
        .name("get_weather")
        .description("获取指定地点的当前天气")
        .parameters(ChatFunctionParameters.builder()
            .type("object")
            .properties(Map.of(
                "location", Map.of(
                    "type", "string",
                    "description", "城市名称"
                )
            ))
            .required(Arrays.asList("location"))
            .build())
        .build())
    .build();

// 创建带函数的请求
ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
    .model(Constants.ModelChatGLM4)
    .messages(Arrays.asList(
        ChatMessage.builder()
            .role(ChatMessageRole.USER.value())
            .content("北京的天气怎么样？")
            .build()
    ))
    .tools(Arrays.asList(weatherTool))
    .toolChoice("auto")
    .build();

ChatCompletionResponse response = client.chat().createChatCompletion(request);
```

### 向量嵌入

```java
import ai.z.openapi.service.embedding.*;

// 创建嵌入请求
EmbeddingCreateParams request = EmbeddingCreateParams.builder()
    .model(Constants.ModelEmbedding3)
    .input(Arrays.asList("你好世界", "你好吗？"))
    .build();

// 执行请求
EmbeddingResponse response = client.embeddings().create(request);

if (response.isSuccess()) {
    response.getData().getData().forEach(embedding -> {
        System.out.println("嵌入向量: " + embedding.getEmbedding());
    });
}
```

### 图像生成

```java
import ai.z.openapi.service.image.*;

// 创建图像生成请求
CreateImageRequest request = CreateImageRequest.builder()
    .model(Constants.ModelCogView3Plus)
    .prompt("山间美丽的日落")
    .size("1024x1024")
    .quality("standard")
    .n(1)
    .build();

// 执行请求
ImageResponse response = client.images().generate(request);

if (response.isSuccess()) {
    response.getData().getData().forEach(image -> {
        System.out.println("图像URL: " + image.getUrl());
    });
}
```

### Spring Boot 集成

```java
@RestController
public class AIController {
    
    private final ZaiClient zaiClient;
    
    public AIController() {
        this.zaiClient = ZaiClient.builder()
            .apiKey("your.api.key.your.api.secret")
            .enableTokenCache()
            .build();
    }
    
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
            .model(Constants.ModelChatGLM4)
            .messages(Arrays.asList(
                ChatMessage.builder()
                    .role(ChatMessageRole.USER.value())
                    .content(request.getMessage())
                    .build()
            ))
            .build();
            
        ChatCompletionResponse response = zaiClient.chat().createChatCompletion(params);
        
        if (response.isSuccess()) {
            String content = response.getData().getChoices().get(0).getMessage().getContent();
            return ResponseEntity.ok(content);
        } else {
            return ResponseEntity.badRequest().body(response.getMsg());
        }
    }
}
```

## 🔧 可用服务

### Chat 服务
- 对话补全（同步/异步）
- 流式对话
- 函数调用
- 多模态对话（文本+图像）

### Embeddings 服务
- 文本向量化
- 批量嵌入
- 多种嵌入模型

### Images 服务
- 文本到图像生成
- 图像编辑
- 图像变体

### Audio 服务
- 语音转文本
- 文本转语音
- 语音翻译

### Files 服务
- 文件上传
- 文件管理
- 文件检索

### Assistants 服务
- AI助手创建
- 助手管理
- 对话线程

### Agents 服务
- 智能代理
- 工作流管理
- 任务执行

### Knowledge 服务
- 知识库管理
- 文档处理
- 知识检索

### Batch 服务
- 批量处理
- 异步任务
- 结果管理

## 🤖 支持的模型

### 文本生成模型
- `glm-4-plus` - 增强版GLM-4，具有更强的能力
- `glm-4` - 标准GLM-4模型
- `glm-4-air` - 轻量级版本，优化速度
- `glm-4-flash` - 超快响应模型
- `glm-4-0520` - GLM-4模型版本0520
- `glm-4-airx` - 扩展Air模型，具有附加功能
- `glm-4-long` - 优化长上下文对话
- `glm-4-voice` - 专为语音交互设计
- `glm-4.1v-thinking-flash` - 具有思维能力的视觉推理模型
- `glm-z1-air` - 优化数学和逻辑推理
- `glm-z1-airx` - 国内最快推理模型，200 tokens/s
- `glm-z1-flash` - 完全免费的推理模型服务
- `glm-4-air-250414` - 通过强化学习优化增强
- `glm-4-flash-250414` - 最新免费语言模型
- `glm-4-flashx` - 增强Flash版本，具有超快推理速度
- `glm-4-9b` - 90亿参数开源模型
- `glm-4-assistant` - 面向各种业务场景的AI助手
- `glm-4-alltools` - 复杂任务规划和执行的代理模型
- `chatglm3-6b` - 60亿参数开源基础模型
- `codegeex-4` - 代码生成和补全模型

### 音频语音识别
- `glm-asr` - 上下文感知音频转录模型

### 实时交互
- `glm-realtime-air` - 具有跨模态推理的实时视频通话模型
- `glm-realtime-flash` - 快速实时视频通话模型

### 视觉模型
- `glm-4v-plus` - 增强视觉模型
- `glm-4v` - 标准视觉模型
- `glm-4v-plus-0111` - 可变分辨率视频和图像理解
- `glm-4v-flash` - 免费且强大的图像理解模型

### 图像生成模型
- `cogview-3-plus` - 增强图像生成
- `cogview-3` - 标准图像生成
- `cogview-3-flash` - 免费图像生成模型
- `cogview-4-250304` - 具有文本功能的高级图像生成
- `cogview-4` - 精确个性化AI图像表达的高级图像生成

### 视频生成模型
- `cogvideox` - 从文本或图像生成视频
- `cogvideox-flash` - 免费视频生成模型
- `cogvideox-2` - 新视频生成模型
- `viduq1-text` - 从文本输入的高性能视频生成
- `viduq1-image` - 从首帧图像和文本描述生成视频
- `viduq1-start-end` - 从首末帧图像生成视频
- `vidu2-image` - 从首帧图像和文本描述的增强视频生成
- `vidu2-start-end` - 从首末帧图像的增强视频生成
- `vidu2-reference` - 使用人物、物体等参考图像的视频生成

### 嵌入模型
- `embedding-3` - 最新嵌入模型
- `embedding-2` - 上一代嵌入模型

### 专业模型
- `charglm-3` - 角色交互模型
- `cogtts` - 文本转语音模型
- `rerank` - 文本重排序和相关性评分
- `emohaa` - 情感分析模型

## 📈 版本更新

详细的版本更新记录和历史信息，请查看 [Release-Note.md](Release-Note.md)。

## 📄 许可证

本项目基于 MIT 许可证开源 - 详情请查看 [LICENSE](LICENSE) 文件。

## 🤝 贡献

欢迎贡献代码！请随时提交 Pull Request。

## 📞 支持

如有问题和技术支持:
- Visit [Z.ai Platform](https://z.ai/)
- Visit [ZHIPU AI Open Platform](http://open.bigmodel.cn/)
- Check our [Architecture Documentation](ARCHITECTURE.md)
