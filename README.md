# Embabel Blog Writer Agent

A Spring Boot application that uses AI agents to automate the creation and review of technical blog posts.

## Purpose

The project is designed to generate high-quality, beginner-friendly blog posts using a two-step agentic process:
1. **Writing**: An AI agent writes an initial draft based on a given topic.
2. **Reviewing**: A separate AI agent reviews the draft, corrects technical errors, and improves the overall writing quality.

## Agents and Personas

The system uses two distinct personas to ensure quality and clarity:

### 1. The Writer
- **Role**: Software Developer and Educator.
- **Goal**: Write practical, beginner-friendly blog posts.
- **Backstory**: An experienced developer who loves teaching through clear, simple writing.
- **Style**: Uses short sentences, plain language, and simple code examples.

### 2. The Reviewer
- **Role**: Technical Editor.
- **Goal**: Review and polish technical blog posts.
- **Backstory**: A seasoned editor focused on clarity, accuracy, and tight writing.
- **Style**: Identifies technical inaccuracies and tightens the prose.

## Configuration

### Environment Variables (.env)

The application requires API keys for the AI models (Anthropic and OpenAI). These should be provided via environment variables.

Create a `.env` file in the project root (or anywhere safe) with the following content:

```env
ANTHROPIC_API_KEY=your_anthropic_api_key_here
OPENAI_API_KEY=your_openai_api_key_here
```

**Note**: Do not commit the `.env` file or your API keys to version control.

### IntelliJ IDEA Setup

To use these environment variables in IntelliJ IDEA:

1. Open **Edit Run Configurations...** (from the Run menu or the toolbar).
2. Select your application's run configuration (e.g., `EmbabelApplication`).
3. Find the **Environment variables** field.
4. Click the folder icon at the end of the field.
5. In the dialog that opens, you can either:
   - Add the variables manually.
   - Use a plugin like "EnvFile" (if installed) to link directly to your `.env` file.
   - Or, simply paste the keys into the environment variables field.

Alternatively, if you are using the **EnvFile** plugin:
1. Go to the **EnvFile** tab in the Run Configuration.
2. Check **Enable EnvFile**.
3. Click the **+** (plus) button and select your `.env` file.

## How it Works

When the application runs, it uses the `BlogWriterAgent` to:
- Prompt the **Writer** to create a draft in Markdown.
- Pass that draft to the **Reviewer**.
- Save the final result as a `.md` file in the `blog-posts/` directory (configurable in `application.properties`).
- Include metadata about the LLMs used, token usage, and calculated costs at the top of the generated file.
