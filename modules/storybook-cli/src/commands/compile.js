import { JSDOM } from 'jsdom';
import fs from 'fs';
import path from 'path';
import { basePath, resolveFile, relativePath } from '../util.js';

function setupJSDOM() {
  try {
    const { window } = new JSDOM();
    global.window = window;
    global.document = window.document;
  } catch (error) {
    throw new Error(`Failed to setup JSDOM: ${error.message}`);
  }
}

async function resolveStoriesCommonJS(outputDir, compilerNs, cljsEntry, target) {
  try {
    // CommonJS dynamic import approach
    await import(resolveFile(outputDir, cljsEntry + '.js'));
    const storybookCljs = await import(
      resolveFile(outputDir, 'io.factorhouse.storybook.core.js')
    );

    return storybookCljs.default.export_stories(
      relativePath(outputDir, '.storybook'),
      compilerNs,
      cljsEntry,
      target
    );
  } catch (error) {
    throw new Error(`Failed to resolve CommonJS stories: ${error.message}`);
  }
}

async function resolveStoriesESM(outputDir, compilerNs, cljsEntry, target) {
  try {
    // ESM requires explicit file:// URLs for absolute paths
    const entryPath = new URL(resolveFile(outputDir, cljsEntry + '.js'), import.meta.url).href;
    const corePath = new URL(resolveFile(outputDir, 'io.factorhouse.storybook.core.js'), import.meta.url).href;

    await import(entryPath);
    const storybookCljs = await import(corePath);

    return storybookCljs.default.export_stories(
      relativePath(outputDir, '.storybook'),
      compilerNs,
      cljsEntry,
      target
    );
  } catch (error) {
    throw new Error(`Failed to resolve ESM stories: ${error.message}`);
  }
}


async function resolveStories(outputDir, compilerNs, cljsEntry, target) {
  if (target === 'esm') {
    return resolveStoriesESM(outputDir, compilerNs, cljsEntry, target);
  } else {
    return resolveStoriesCommonJS(outputDir, compilerNs, cljsEntry, target);
  }
}

function ensureStorybook() {
  const resolved_path = path.resolve(basePath(), '.storybook/');
  if (fs.existsSync(resolved_path)) {
    return true;
  } else {
    throw new Error(
      `Failed to compile stories: directory ${resolved_path} does not exist.`
    );
  }
}

export async function compile(compilerNs, outputDir, jsOutDir, cljsEntry, target = 'npm-module') {
  console.time(`storybook-compile-${target}`);
  ensureStorybook();
  setupJSDOM();

  const to_export = await resolveStories(outputDir, compilerNs, cljsEntry, target);
  to_export.forEach(([fileName, content]) => {
    const filePath = resolveFile(jsOutDir, fileName);
    const dirPath = path.dirname(filePath);
    fs.mkdirSync(dirPath, { recursive: true });
    fs.writeFileSync(filePath, content, 'utf8');
  });
  console.timeEnd(`storybook-compile-${target}`);
}
