import path from 'path';
import fs from 'fs';

function findPackageJsonDir(startDir) {
  let currentDir = startDir;

  while (currentDir !== path.parse(currentDir).root) {
    if (fs.existsSync(path.join(currentDir, 'package.json'))) {
      return currentDir;
    }
    currentDir = path.dirname(currentDir);
  }

  throw new Error('package.json not found');
}

export function basePath() {
  return findPackageJsonDir(process.cwd());
}

export function resolveFile(dir, fileName) {
  return path.resolve(basePath(), dir, fileName);
}

export function relativePath(fullPath, baseDir) {
  const normalized = path.normalize(fullPath);
  const base = path.normalize(baseDir);

  if (normalized.startsWith(base)) {
    const remainder = normalized.slice(base.length);
    return remainder.replace(/^[/\\]/, '');
  }

  return normalized;
}
