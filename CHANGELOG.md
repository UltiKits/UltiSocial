# Changelog

All notable changes to this project are documented in this file.
Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

本文件记录本项目的所有重要更改，格式基于 [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)。

## [Unreleased]

### Fixed

- Reloading this module (`/ul reload UltiSocial`, or `/ul reload` for every module) now re-reads
  `config/social.yml` and refreshes the language files, so an edited value such as
  `tp_to_friend.enabled` applies to the next command without a restart. The framework's reload
  already performed both steps, but this module's reload method replaced it and only logged a line,
  so neither step ran and the reload reported success without reloading anything. UltiTools 6.3.0
  also runs its `@ConditionalOnConfig` drift check at this point (this module has no conditional
  beans, so it reports nothing) and logs its own per-module reload line (UltiKits/UltiSocial#13).
- After `/upm uninstall UltiSocial`, the module's `/friend` command (with its `/friends` and `/f`
  aliases) is now really removed and its listeners (friend online/offline notifications and the
  friend-list and block-list GUI clicks) stop firing; this module has no unload work of its own.
  Previously both stayed active until the server restarted (UltiKits/UltiSocial#13).
- 重载本模块（`/ul reload UltiSocial`，或对所有模块执行 `/ul reload`）现在会重新读取 `config/social.yml`
  并刷新语言文件，修改后的 `tp_to_friend.enabled` 等配置无需重启即可对下一条命令生效。框架的重载本已执行这两步，
  但本模块的重载方法替换了它且只输出一行日志，因此这两步都不会执行，重载报告成功却什么也没有重载。
  UltiTools 6.3.0 还会在此时执行 `@ConditionalOnConfig` 漂移检查（本模块没有条件注册的 Bean，因此不会报告任何
  内容）并输出框架自身的模块重载日志（UltiKits/UltiSocial#13）。
- 执行 `/upm uninstall UltiSocial` 后，本模块的 `/friend` 命令（及其 `/friends`、`/f` 别名）现在会被真正移除，
  其监听器（好友上线/下线通知，以及好友列表与黑名单界面的点击）也不再触发；本模块自身没有卸载工作。
  此前两者都会一直生效到服务器重启（UltiKits/UltiSocial#13）。

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
- The `notifications.friend_join_world` setting in `config/social.yml` (default `false`, described
  as "Notify when friend joins your world"). It never had any effect in any version: nothing read
  it, and this module has no notification for a friend entering your world at all, so switching it
  on or off never changed anything a player saw. The notification is recorded as a feature request,
  UltiKits/UltiSocial#21 — removing the setting does not reject the feature. A server upgraded from
  an earlier version keeps the key in its `social.yml`, because the framework never deletes a key
  from an operator's file; the key can simply be deleted, and leaving it there changes nothing
  players see (UltiKits/UltiSocial#15).
- 移除 `config/social.yml` 中的 `notifications.friend_join_world` 设置项（默认 `false`，说明为"好友进入你所在
  世界时通知"）。它在任何版本中都从未生效：没有任何代码读取它，本模块也根本没有"好友进入你所在世界"的通知，
  因此开启或关闭它从未改变玩家看到的任何内容。该通知已作为功能请求记录在 UltiKits/UltiSocial#21——删除设置
  并不代表否决该功能。从旧版本升级的服务器，其 `social.yml` 中仍会保留该键，因为框架从不删除运维文件中的键；
  直接删除该键即可，保留它也不会改变玩家看到的任何内容（UltiKits/UltiSocial#15）。
