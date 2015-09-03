# Contributing to jLibBig

## Issue tracking

## Snippets

## Branch organisation

Master branch: master
Develop branch: develop

* Prefixes
Release branches: release/<version>
Feature branches: feature/<name>-<target version, if given>
Hot-fix branches: hotfix/<version>
Support branches: support/<legacy version>

A short and nice tutorial about this branching model
can be found [here](http://nvie.com/posts/a-successful-git-branching-model/).
Utilities such [git-flow](https://github.com/nvie/gitflow) may be very helpful.

### Versions

Every commit to master is a new version by definition and must be tagged with its version.

We adopt a three stages numbering a.b.c where c is reserved for hot-fixes and should be omitted when 0, e.g. in the case of releases adding new features. Versions meant to be internal or candidate releases have an additional suffix separated by "-" e.g. "1.0-rc1".

### Release branches
Checklist:
1. branch from develop;
2. must merge back into both master and develop;
3. follow namining conventions: release-<version>;
4. commit to master must be tagged with the version.

### Feature branches
Checklist:
1. branch from develop;
2. must merge back into develop (not master!);
3. follow naming conventions: feature-<name>-<target version, if given>.

### Hot-fixes branches
Checklist:
1. branch from master;
2. must merge back into both master and develop;
3. follow namining conventions: hotfix-<version>;
4. commit to master must be tagged with the version.

