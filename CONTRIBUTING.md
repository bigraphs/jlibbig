# How to contribute to jLibBig

Thanks for considering contributing to jLibBig.

## Support questions

Before you rush to the issue tracker, please check whether
the project wiki and snippets can help with your issue; 
you may also consider contacting one of the developers.
If your problem is not strictly related to jLibBig, then
communities like stackoverflow are a great place to start asking.  
When all of the above fails to help, 
please report the issue using the tracker (see below).

## Reporting issues
 Under which versions of jLibBig does this happen? Check if this issue is fixed in the repository.

When you report an issue:
* isolate the issue as far as possible;
* provide any detail you feel could help us reproduce the issue.
isolate the issue in order to help us reproduce it;
* a concise example/snippet that reproduces the issue is always welcome and of great help to us in order to solve the issue more quickly.  

## Submitting patches
Include tests if your patch is supposed to solve a bug, and explain clearly under which circumstances the bug happens. 
Make sure the test fails without your patch.

# Devs guidelines

## Branch organisation

*Master branch: master
*Develop branch: develop

* Prefixes
**Release branches: release/version
**Feature branches: feature/name-target version(if given)
**Hot-fix branches: hotfix/version
**Support branches: support/legacy version

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
3. follow namining conventions;
4. commit to master must be tagged with the version.

### Feature branches
Checklist:
1. branch from develop;
2. must merge back into develop (not master!);
3. follow naming conventions.

### Hot-fixes branches
Checklist:
1. branch from master;
2. must merge back into both master and develop;
3. follow namining conventions;
4. commit to master must be tagged with the version.

