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

async function resolveStories(outputDir, compilerNs, cljsEntry, target) {
  try {
    const entryFile = resolveFile(outputDir, cljsEntry + '.js');
    const coreFile = resolveFile(outputDir, 'io.factorhouse.storybook.core.js');
    let storybookCljs = null;
    if (target === 'npm-module') {
      await import(entryFile);
      storybookCljs = await import(coreFile);
    } else if (target === 'esm') {
      await import(new URL(entryFile).href);
      storybookCljs = await import(new URL(coreFile).href);
    } else {
      throw new Error(`Unknown target: ${target}`);
    }
    return storybookCljs.default.export_stories(
      relativePath(outputDir, '.storybook'),
      compilerNs,
      cljsEntry,
      target
    );
  } catch (error) {
    throw new Error(`Failed to resolve stories: ${error.message}`);
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
  console.time('storybook-compile');
  ensureStorybook();
  setupJSDOM();

  const to_export = await resolveStories(outputDir, compilerNs, cljsEntry, target);
  to_export.forEach(([fileName, content]) => {
    const filePath = resolveFile(jsOutDir, fileName);
    const dirPath = path.dirname(filePath);
    fs.mkdirSync(dirPath, { recursive: true });
    fs.writeFileSync(filePath, content, 'utf8');
  });
  console.timeEnd('storybook-compile');
}
