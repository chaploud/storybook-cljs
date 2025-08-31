#!/usr/bin/env node
import { Command } from 'commander';
import { init } from './commands/init.js';
import { compile } from './commands/compile.js';

const program = new Command();

program
  .name('storybook-cljs')
  .description('CLI tool for ClojureScript Storybook integration')
  .version('1.0.0');

program
  .command('init')
  .description('Create the boilerplate for ClojureScript Storybook integration')
  .action(init);

program
  .command('compile')
  .argument('<string>', 'compilerNs')
  .argument('<string>', 'outDir')
  .argument('<string>', 'jsOutDir')
  .argument('<string>', 'entryNs')
  .argument('<string>', 'target')
  .description('Compile Storybook.js from ClojureScript source')
  .action(compile);

program.parse();
