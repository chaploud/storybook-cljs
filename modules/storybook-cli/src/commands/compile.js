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

async function importClojureScriptModule(filePath, isESM) {
  if (isESM) {
    return await import(new URL(filePath, import.meta.url).href);
  } else {
    return await import(filePath);
  }
}

async function resolveStories(outputDir, compilerNs, cljsEntry, target) {
  const isESM = target === 'esm';
  const paths = {
    entry: resolveFile(outputDir, cljsEntry + '.js'),
    core: resolveFile(outputDir, 'io.factorhouse.storybook.core.js')
  };

  try {
    await importClojureScriptModule(paths.entry, isESM);
    const storybookCljs = await importClojureScriptModule(paths.core, isESM);

    return storybookCljs.default.export_stories(
      relativePath(outputDir, '.storybook'),
      compilerNs,
      cljsEntry,
      target
    );
  } catch (error) {
    throw new Error(`Failed to resolve ${target} stories from ${outputDir}: ${error.message}`);
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
