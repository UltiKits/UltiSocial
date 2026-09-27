# Changelog

All notable changes to this project are documented in this file.
Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

本文件记录本项目的所有重要更改，格式基于 [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)。

## [Unreleased]

### Added

- A warning for a setting this version no longer reads. If `notifications.friend_join_world` (removed,
  see `### Removed`) is still in your `config/social.yml`, which it will be on any server that has run
  an earlier version, the module logs one warning when it is enabled and on every reload of it
  (`/ul reload` or `/ul reload UltiSocial`), naming the module, the file and the key and saying that
  nothing replaces it. Deleting the key from the file silences the warning; leaving it there changes
  nothing else (UltiKits/UltiSocial#15).
- 新增对本版本已不再读取的设置项的警告。若已移除的 `notifications.friend_join_world`（见 `### Removed`）仍保留在
  你的 `config/social.yml` 中（运行过旧版本的服务器都会如此），本模块会在启用时以及每次重载本模块时
  （`/ul reload` 或 `/ul reload UltiSocial`）输出一条警告，指明模块、文件和键，并说明没有其他设置取代它。
  从文件中删除该键即可消除警告；保留它不会产生其他任何影响（UltiKits/UltiSocial#15）。

### Changed

- Message and title settings in `config/social.yml` — the friend-list title (`gui_title`) and
  the ten messages (`messages.friend_added`, `friend_removed`, `friend_online`, `friend_offline`,
  `request_sent`, `request_received`, `request_denied`, `max_friends_reached`, `already_friends`,
  `blocked`) — are written in the server's language when the module starts, and the file is what the
  module shows (for example `messages.friend_added` holds `&aYou and {PLAYER} are now friends!` under
  `language: en`); previously they were fixed Chinese text, so `language: en` had no effect on them
  (UltiKits/UltiSocial#14). A setting that is still built-in text — in any language, or a default an
  earlier version shipped — follows `language`: it is rewritten when the module starts or after
  `/ul reload`. A setting you edited is kept. To keep a built-in text but stop it following
  `language`, change at least one character. The text written is this module's built-in text: edit
  these settings in `config/social.yml`; an edit of the extracted language file does not change them
  (earlier versions never read them from the language file either).
- `config/social.yml` 中的消息与标题设置——好友列表标题（`gui_title`）与十条消息（`messages.friend_added`、
  `friend_removed`、`friend_online`、`friend_offline`、`request_sent`、`request_received`、`request_denied`、
  `max_friends_reached`、`already_friends`、`blocked`）——在模块启动时按服务器语言写入，文件内容即模块显示的内容；
  此前它们是写死的中文，`language: en` 对它们不起作用（UltiKits/UltiSocial#14）。仍为内置文本（任一语言的内置文本，
  或旧版本的出厂默认值）的设置会跟随 `language`：模块启动或执行 `/ul reload` 后改写为当前语言的文本。你改过的设置
  保持不变。若想保留内置文本又不让它跟随语言，请至少改动一个字符。写入的是本模块的内置文本：请在 `config/social.yml`
  中修改这些设置；修改已解压的语言文件不会改变它们（旧版本同样从不从语言文件读取它们）。

### Fixed

- `language: en` now applies to everything this module shows or logs: every `/friend` reply and the
  help, both GUIs (titles, lore, game-mode names, click tips, buttons), the replies to GUI clicks, the
  command description, and the console lines. Most of this was fixed Chinese text in every language,
  although the language files already held English text for it that no code read; the console lines
  were fixed English text and now follow `language: zh` too (UltiKits/UltiSocial#14). A private
  message's own words are shown exactly as typed. The enable line now reads
  `UltiSocial has been enabled!`; it used to name a version, v1.1.0, that this module never had.
- `language: en` 现在对本模块显示或记录的全部内容生效：`/friend` 的所有回复与帮助、两个界面（标题、说明、游戏模式名称、
  点击提示、按钮）、界面点击的回复、命令描述以及控制台日志。其中大部分原先在任何语言下都是写死的中文，而语言文件中其实已有
  无人读取的英文文本；控制台日志原先写死为英文，现在也跟随 `language: zh`（UltiKits/UltiSocial#14）。私聊消息本身按原样显示。
  启用日志现为 `UltiSocial has been enabled!`，此前其中写的版本号 v1.1.0 本模块从未有过。

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

- `messages.player_blocked` and `messages.player_unblocked` never took effect and have been removed;
  they can be deleted from existing `config/social.yml` files. Nothing ever read them: `/friend block`
  and `/friend unblock` replied with fixed text, and now reply from the language file (`lang/en.yml`,
  `lang/zh.yml`), which is where to change those replies. A file that still holds either key gets
  the same warning as `notifications.friend_join_world`, at start-up and on every reload of the
  module (UltiKits/UltiSocial#23).
- `messages.player_blocked` 与 `messages.player_unblocked` 从未生效，现已移除，可从现有的 `config/social.yml` 中删除。
  从未有代码读取它们：`/friend block` 与 `/friend unblock` 的回复原为写死的文本，现在来自语言文件（`lang/en.yml`、
  `lang/zh.yml`），要修改这两条回复请改那里。仍含其中任一键的文件，会在启动时以及每次重载本模块时收到与
  `notifications.friend_join_world` 相同的警告（UltiKits/UltiSocial#23）。

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
