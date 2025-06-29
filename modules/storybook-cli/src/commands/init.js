import fs from 'fs';
import path from 'path';
import { spawn } from 'child_process';
import { fileURLToPath } from 'url';
import readline from 'readline';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const devDeps = {
  '@storybook/react': '9.0.14',
  '@storybook/react-webpack5': '9.0.14',
  'os-browserify': '^0.3.0',
  'tty-browserify': '^0.0.1',
  storybook: '9.0.14',
};

function prompt(question) {
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
  });

  return new Promise((resolve) => {
    rl.question(question, (answer) => {
      rl.close();
      resolve(answer.toLowerCase().trim());
    });
  });
}

function runNpmInstall() {
  return new Promise((resolve, reject) => {
    console.log('Running npm install...');
    const npm = spawn('npm', ['install'], { stdio: 'inherit' });

    npm.on('close', (code) => {
      if (code === 0) {
        resolve();
      } else {
        reject(new Error(`npm install failed with code ${code}`));
      }
    });
  });
}

export async function init() {
  try {
    const cwd = process.cwd();
    const packageJsonPath = path.join(cwd, 'package.json');
    const storybookDir = path.join(cwd, '.storybook');
    const storybookMainPath = path.join(storybookDir, 'main.js');
    const sourceMainPath = path.join(__dirname, '..', 'init', 'main.js');
    const storybookPreviewPath = path.join(storybookDir, 'preview.js');
    const sourcePreviewPath = path.join(__dirname, '..', 'init', 'preview.js');

    if (!fs.existsSync(packageJsonPath)) {
      console.error('❌ No package.json found in current directory.');
      console.log('Please run "npm init" first to create a package.json file.');
      process.exit(1);
    }

    if (fs.existsSync(storybookMainPath)) {
      throw new Error(
        '❌ .storybook/main.js already exists. Aborting to avoid overwriting.'
      );
    }

    if (fs.existsSync(storybookPreviewPath)) {
      throw new Error(
        '❌ .storybook/preview.js already exists. Aborting to avoid overwriting.'
      );
    }

    console.log('\n📋 Initialization Plan:');
    console.log('1. ✓ package.json found');
    console.log('2. Add Storybook dev dependencies to package.json');
    console.log('3. Run npm install');
    console.log('4. Create .storybook directory (if needed)');
    console.log(
      '5. Copy template {main|preview}.js to .storybook/{main|preview}.js'
    );

    console.log('\n📦 Dev dependencies to be added:');
    Object.entries(devDeps).forEach(([pkg, version]) => {
      console.log(`  ${pkg}: ${version}`);
    });

    const answer = await prompt('\n❓ Proceed with initialization? (y/N): ');

    if (answer !== 'y' && answer !== 'yes') {
      console.log('❌ Initialization cancelled.');
      return;
    }

    console.log('\n🚀 Starting initialization...');

    console.log('📝 Updating package.json...');
    const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));

    if (!packageJson.devDependencies) {
      packageJson.devDependencies = {};
    }

    Object.assign(packageJson.devDependencies, devDeps);
    fs.writeFileSync(
      packageJsonPath,
      JSON.stringify(packageJson, null, 2) + '\n'
    );
    console.log('✓ Added dev dependencies to package.json');

    await runNpmInstall();
    console.log('✓ npm install completed');

    if (!fs.existsSync(storybookDir)) {
      fs.mkdirSync(storybookDir, { recursive: true });
      console.log('✓ Created .storybook directory');
    } else {
      console.log('✓ .storybook directory already exists');
    }

    if (!fs.existsSync(sourceMainPath)) {
      throw new Error(`❌ Source file not found: ${sourceMainPath}`);
    }

    if (!fs.existsSync(sourcePreviewPath)) {
      throw new Error(`❌ Source file not found: ${sourcePreviewPath}`);
    }

    fs.copyFileSync(sourceMainPath, storybookMainPath);
    console.log('✓ Copied main.js to .storybook/main.js');

    fs.copyFileSync(sourcePreviewPath, storybookPreviewPath);
    console.log('✓ Copied preview.js to .storybook/preview.js');

    console.log('\n🎉 Initialization completed successfully!');
  } catch (error) {
    console.error('\n💥 Initialization failed:', error.message);
    process.exit(1);
  }
}
