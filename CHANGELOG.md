# Changelog

All notable changes to this project are documented in this file.
Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

本文件记录本项目的所有重要更改，格式基于 [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)。

## [Unreleased]

### Fixed

- Reloading this module (`/ul reload UltiSocial`, or `/ul reload` for every module) now re-reads
  `config/social.yml` and refreshes the language files, so an edited value such as
  `tp_to_friend.enabled` applies to the next command without a restart. Previously this module's
  reload method replaced the framework's and only logged a line, so neither step ran and the reload
  reported success without reloading anything. UltiTools 6.3.0 also runs its `@ConditionalOnConfig`
  drift check at this point (this module has no conditional beans, so it reports nothing) and logs
  its own per-module reload line (UltiKits/UltiSocial#13).
- Unloading this module (`/upm uninstall UltiSocial`, or server shutdown) now runs the framework's
  command unregistration, then its listener unregistration; this module has no unload work of its
  own. Previously this module's unload method replaced the framework's and only logged a line, so
  its commands were never unregistered on any unload path, and its listeners were not unregistered
  on `/upm uninstall` (UltiKits/UltiSocial#13).
- 重载本模块（`/ul reload UltiSocial`，或对所有模块执行 `/ul reload`）现在会重新读取 `config/social.yml`
  并刷新语言文件，修改后的 `tp_to_friend.enabled` 等配置无需重启即可对下一条命令生效。此前本模块的重载方法
  替换了框架的重载方法且只输出一行日志，这两步都不会执行，重载报告成功却什么也没有重载。UltiTools 6.3.0
  还会在此时执行 `@ConditionalOnConfig` 漂移检查（本模块没有条件注册的 Bean，因此不会报告任何内容）并输出
  框架自身的模块重载日志（UltiKits/UltiSocial#13）。
- 卸载本模块（`/upm uninstall UltiSocial` 或关闭服务器）现在会先由框架注销命令，再注销监听器；本模块自身
  没有卸载工作。此前本模块的卸载方法替换了框架的卸载方法且只输出一行日志，因此任何卸载途径都不会注销其命令，
  `/upm uninstall` 也不会注销其监听器（UltiKits/UltiSocial#13）。

### Removed

- The module's own console lines `UltiSocial has been disabled!` (on unload) and
  `UltiSocial configuration reloaded!` (on `/ul reload UltiSocial` or `/ul reload`). Both were
  English literals, not language keys; the never-consulted `social_disabled` language key, which
  described the unload line, is removed from both language files. UltiTools 6.3.0 logs one reload
  line per module (`Module 'UltiSocial' reloaded.`) (UltiKits/UltiSocial#13).
- 移除本模块自身的控制台行 `UltiSocial has been disabled!`（卸载时）和 `UltiSocial configuration reloaded!`
  （`/ul reload UltiSocial` 或 `/ul reload` 时）。两者均为英文字面量而非语言键；同时从两个语言文件中移除描述
  卸载行但从未被使用的 `social_disabled` 语言键。UltiTools 6.3.0 会为每个模块输出一行重载日志
  （`Module 'UltiSocial' reloaded.`）（UltiKits/UltiSocial#13）。
