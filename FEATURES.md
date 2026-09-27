# UltiSocial — Feature Inventory

This document catalogues every operator- or player-visible function, command, content item and
configuration key in this repository, as read directly from source. It is an internal reference
for UAT execution and issue reconciliation — the public description of these features lives on
<https://doc.ultikits.com/>. Update this file in the same pull request as any feature change.

## Conventions

- **ID grammar:** `<repo-slug>.<area>.<action>`, dot-separated, every segment lowercase ASCII
  drawn from `[a-z0-9-]`. `<repo-slug>` is the repository name lowercased with no separators —
  `ultisocial` here, `ultichat`, `ultitools`, `ultitools-example` for
  `UltiTools-External-Example`. `<area>` is the feature section's slug. `<action>` is the verb.
  A `config` row is the one shape that exceeds three segments and is exempt from the
  lowercase-ASCII rule for its key-path suffix:
  `<repo-slug>.config.<file-stem>.<yml key path>`, the key path keeping its own dots and its own
  casing verbatim from the yml file. An ID changes only when the feature's identity changes,
  never on rewording. IDs are unique within a repository.
- **Kind**, exactly these eight values: `command`, `config`, `event`, `gui`, `scheduled`,
  `placeholder`, `persistence`, `gate`. Each maps one-to-one onto a reconciliation-table line.
  This module has no `placeholder` rows (no PlaceholderAPI usage anywhere in source) and no
  `gate` rows (its `@ConditionalOnConfig` count is 0) — both Kinds stay in the vocabulary for
  cross-repository consistency even though neither appears below.
- **Tier**, exactly three: `player`, `admin`, `internal`. Judged from what the feature is for.
  This module is the ecosystem's clearest `player`-tier surface: friend requests, blocks, and the
  lists themselves are things any player uses directly on themselves — there is no admin-facing
  command anywhere in `FriendCommand` (no `requireOp`, no cross-player admin variant the way
  UltiBackup's `/backup admin ...` has one). Every `command` row below is `player` tier.
- **Manual**, exactly three: `detailed`, `brief`, `none`.
- **Target**, exactly four: `player`, `console`, `both`, or `n/a`. `FriendCommand`'s class-level
  `@CmdTarget(CmdTargetType.PLAYER)` makes every one of its 13 `@CmdMapping` sites `player` —
  there is no console-usable command in this module.
- **Permission:** the literal node string, `none`, or `n/a`. `FriendCommand` declares one
  class-level permission (`ultisocial.use`) with no `requireOp` and no per-`@CmdMapping`
  override (grep confirms zero uses of `@CmdMapping(permission=)` in this class), so every
  command row below reads `ultisocial.use` with no suffix.
- **Source:** `ClassName#member` — the class and member that actually reads or applies the
  feature — for every Kind, `config` included: all 17 `config` rows below cite the reading member,
  not `SocialConfig`'s own field declaration.
- **Row order:** by section, then by ID ascending within the section.
- **No manual prose:** no troubleshooting column, no explanatory paragraphs, no draft page text.
  A hazard noticed while reading becomes a negative checklist row, not a note here. Where a
  feature's actual runtime behaviour genuinely diverges from what its own shipped language
  catalogue implies it does (a dead lang key, a config key with no reader), that fact is stated
  here as a plain, sourced observation, with the filed issue number.

### Language

Every chat line, GUI title, lore line and click tip, and every console line this module writes
comes from its language file (`lang/en.yml`, `lang/zh.yml`), so it follows the framework's
`language` setting. The friend-list title and the ten `messages.*` settings of `config/social.yml`
are written into that file in the server's language while they are still built-in text, and the
module shows exactly what the file holds (`ultisocial.lifecycle.legacy-text-defaults`); an edited
value is kept. On upgrade, a value that is exactly the Chinese default an earlier version shipped is
rewritten in the server's language at start-up and on every reload. These settings are edited in
`config/social.yml`; an edit of the extracted language file does not change them (earlier versions
never read them from the language file either). Before 6.3.0 only five of the catalogue's keys were
read and everything else was fixed Chinese text (`UltiKits/UltiSocial#14`).

### Reconciliation command family

The canonical form for counting an annotation site across this repository's real sources:

```bash
find <repo-root> -path '*/src/main/java/*' -name '*.java' -not -path '*/target/*' \
  -not -path '*/.worktrees/*' -print0 | xargs -0 grep -nE '^[[:space:]]*@AnnotationName\b' | wc -l
```

This form defeats three measured traps, each of which produces a wrong-but-plausible number
rather than an error: multi-root repositories (UltiBot), git worktrees/build output
(UltiEconomy's `.worktrees/economy-v2/`), and javadoc/string-literal mentions of an annotation
name (defeated by the `^[[:space:]]*@` line-start anchor). None of the three traps applies to
this single-root, worktree-free module, but the robust `find` form is used regardless — the same
command must work unmodified across all 18 repositories.

**GUI page classes** are found structurally, not by grepping for an annotation — neither
`FriendListGUI` nor `BlockListGUI` carries a page-marking annotation; both are plain
`InventoryHolder` implementations under the module's own `gui` package:

```bash
find <repo-root>/src/main/java -path '*/gui/*' -name '*.java' -not -path '*/target/*' | wc -l
```

**Positive control:** the line-start form returns `@CmdExecutor` = 1, `@CmdMapping` = 13,
`@EventListener` = 1 (class), `@EventHandler` = 3 (handler methods), `@Scheduled` = 1,
`@ConditionalOnConfig` = 0, `@ConfigEntity` = 1, `@ConfigEntry` = 17, `@Table` = 2 — confirmed by
reading `FriendCommand.java` directly (13 `@CmdMapping` sites at lines 54, 60, 78, 94, 99, 104, 109, 126, 172, 219, 255, 264, 272) and `SocialConfig.java` (17 `@ConfigEntry` sites; 20 before
`UltiKits/UltiSocial#15` removed `notifications.friend_join_world`, 19 before
`UltiKits/UltiSocial#23` removed `messages.player_blocked` and `messages.player_unblocked`). The
`find`-based GUI-class count above returns 2, matching the module's independently-derived
list of GUI classes excluded from the coverage gate (`FriendListGUI`, `BlockListGUI`).
This document's command-row count matches the `@CmdMapping` annotation-site count exactly (13
against 13).

**Reconciliation note — event Kind (3 `@EventHandler` methods, 2 of them catalogued as `event`
rows; plus 2 `event` rows with no handler; 4 `event` rows in total):** the totals are not a 1:1
match, the same shape as UltiBackup's own reconciliation note.
`SocialListener` carries 3 `@EventHandler` methods; only `onPlayerJoin` and `onPlayerQuit` are
catalogued as `event`-Kind rows in `## Player Presence Notifications` below. The third
(`onInventoryClick`, which dispatches to two private handlers for the two GUI classes) is the
click-routing implementation *for* this module's `gui`-Kind rows, not an independent
player-visible behaviour of its own — named, by method, in each `gui`-Kind row's own Feature
text in `## GUI` below. The third and fourth `event`-Kind rows, `ultisocial.lifecycle.reload` and
`ultisocial.lifecycle.removed-key-warning` in `## Lifecycle Hooks`, have no `@EventHandler` of their
own: they are `event`-Kind because the framework's module enable and `/ul reload` drive them, not a
command this repository maps.

## Friend Management Commands

`FriendCommand` — class-level `@CmdExecutor(alias = {"friend", "friends", "f"}, permission =
"ultisocial.use", description = "command_description")` — a language key the framework translates
("Friend system" under `language: en`) — and `@CmdTarget(PLAYER)`.

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.friend.accept | Accept a pending friend request by the sender's name, creating a BIDIRECTIONAL friendship (one `FriendshipData` row for each side); refuses if the sender's own friend count is already at `max_friends`. Its outcome messages come from the language file, the friend-added one through `messages.friend_added` (the file's text, written in the server's language) | command | `/friend accept <player>` | ultisocial.use | player | player | brief | FriendCommand#acceptRequest, FriendService#acceptRequest |
| ultisocial.friend.add | Send a friend request to an online player; auto-accepts immediately if the target had already sent a request to the sender (mutual request collapses into an instant friendship, calling `acceptRequest` internally rather than creating a second pending request). Every reply follows `language`: the not-online and self-add refusals and the already-sent refusal come from the language file, and the blocked, already-friends, max-friends, sent and received messages through their `messages.*` settings (the file's text, written in the server's language) | command | `/friend add <player>` | ultisocial.use | player | player | brief | FriendCommand#addFriend, FriendService#sendRequest |
| ultisocial.friend.blocklist | Open the blacklist management GUI (`ultisocial.gui.block-list`) for the sender's own blacklist | command | `/friend blocklist` | ultisocial.use | player | player | brief | FriendCommand#openBlockList |
| ultisocial.friend.block | Add a player (online or a known offline player) to the sender's blacklist; automatically removes any existing friendship with that player (both directions). Its replies come from the language file (`player_blocked`, `already_blocked`, `cannot_block_self`, `player_not_exist`); the configuration key `messages.player_blocked`, which nothing ever read, is removed (`UltiKits/UltiSocial#23`) | command | `/friend block <player>` | ultisocial.use | player | player | brief | FriendCommand#blockPlayer, FriendService#addToBlacklist |
| ultisocial.friend.deny | Deny a pending friend request by the sender's name, removing it from the pending queue without creating a friendship. Outcome messages come from the language file (`no_pending_request`, `request_not_exist`) or `messages.request_denied` (the file's text, written in the server's language) | command | `/friend deny <player>` | ultisocial.use | player | player | brief | FriendCommand#denyRequest, FriendService#denyRequest |
| ultisocial.friend.help | Print the `/friend` command usage summary, including the blacklist sub-section; fourteen lines from the language file (`help_title` through `help_blocklist`) | command | `/friend help` | ultisocial.use | player | player | none | FriendCommand#help |
| ultisocial.friend.list | List the sender's own friends, sorted favorites-first then alphabetically, each with an online/offline indicator; every line comes from the language file | command | `/friend list` | ultisocial.use | player | player | brief | FriendCommand#listFriends |
| ultisocial.friend.msg | Send a private message to an online friend; refuses a non-friend target, an offline target, or an effectively-empty message (whitespace-only after trimming). Every refusal and the private-message prefix (`[PM]` under `language: en`) come from the language file; the message itself is shown exactly as typed | command | `/friend msg <player> <message...>` | ultisocial.use | player | player | brief | FriendCommand#sendMessage |
| ultisocial.friend.open | Open the paginated friend list GUI (`ultisocial.gui.friend-list`) for the sender's own friends; this is the module's default (bare-argument) command | command | `/friend` (bare, no arguments) | ultisocial.use | player | player | brief | FriendCommand#openFriendList |
| ultisocial.friend.remove | Remove an existing friendship, removing BOTH sides' `FriendshipData` rows (the sender's own row by ID, and the reverse row via a `WHERE`-matched delete). Outcome messages come from the language file (`not_friend`) or `messages.friend_removed` (the file's text, written in the server's language) | command | `/friend remove <player>` | ultisocial.use | player | player | brief | FriendCommand#removeFriend, FriendService#removeFriend |
| ultisocial.friend.requests | List the sender's own pending (received) friend requests, each with an inline hint naming the exact `/friend accept` command to run; every line comes from the language file | command | `/friend requests` | ultisocial.use | player | player | brief | FriendCommand#viewRequests |
| ultisocial.friend.tp | Teleport the sender to an online friend, subject to `tp_to_friend.enabled` and a per-sender cooldown (`tp_to_friend.cooldown`); uses the framework's `TeleportService` if a bean is available, otherwise falls back to `Player#teleport` directly. Every reply (disabled, not a friend, offline, cooldown, success) comes from the language file | command | `/friend tp <player>` | ultisocial.use | player | player | brief | FriendCommand#teleportToFriend, FriendService#canTeleport |
| ultisocial.friend.unblock | Remove a player from the sender's blacklist by name; both replies come from the language file (`player_unblocked`, `not_in_blocklist`); the configuration key `messages.player_unblocked`, which nothing ever read, is removed (`UltiKits/UltiSocial#23`) | command | `/friend unblock <player>` | ultisocial.use | player | player | brief | FriendCommand#unblockPlayer, FriendService#removeFromBlacklist |

## GUI

Two GUI page classes, neither carrying a page-marking annotation — identified structurally (see
Conventions' own reconciliation note for this Kind). Both are the complete set of this
module's GUI classes excluded from the coverage gate.

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.gui.block-list | Paginated (45-per-page) chest GUI listing the viewer's blacklist, each entry showing the blocked player's skull, block time, and optional reason; left-click unblocks; a Back button (slot 47) returns to `ultisocial.gui.friend-list` (this GUI's OWN Back button, not a route the friend-list offers into this one). An empty-state item (slot 22) appears only when the blacklist is empty. Click routing lives entirely in `SocialListener#onInventoryClick`/`#handleBlockListClick`, not in this class. Every string (title, lore, click tips, buttons) comes from the language file | gui | `ultisocial.friend.blocklist` (the ONLY way in — `FriendListGUI` has no button of its own that opens this GUI; the Back button described above is the reverse direction) | n/a | n/a | player | brief | BlockListGUI#updateInventory, SocialListener#onInventoryClick, SocialListener#handleBlockListClick |
| ultisocial.gui.friend-list | Paginated (45-per-page) chest GUI listing the viewer's own friends (favorites first, then alphabetical), each entry a player-head skull showing online/offline status, world and game mode (if online), add time, and per-entry click tips; left-click teleports to an online friend (if enabled and off cooldown), shift+left-click toggles favorite, right-click messages an online friend (prints the exact command to run) or removes an offline one, shift+right-click always removes. A pending-requests button (slot 47) appears only when the viewer has at least one pending request, and opening it runs `/friend requests` via `Player#performCommand`. Click routing lives entirely in `SocialListener#onInventoryClick`/`#handleFriendListClick`, not in this class. The GUI's own title comes from `gui_title` (the file's text, written in the server's language from the jar's catalogue key `gui_friend_list`); every other string (lore, game-mode names, click tips, buttons) comes from the language file | gui | `ultisocial.friend.open` | n/a | n/a | player | brief | FriendListGUI#updateInventory, SocialListener#onInventoryClick, SocialListener#handleFriendListClick |

## Player Presence Notifications

`SocialListener` — class-level `@EventListener`. Two of this class's three `@EventHandler`
methods are catalogued here (see the Conventions section's own reconciliation note for the
third, which is `ultisocial.gui.*`'s own click-routing implementation).

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.event.friend-offline-notify | Notify every online friend of a quitting player, using `NotificationService` if a bean is available, otherwise a plain chat message; also unconditionally clears the quitting player's friend/blacklist caches regardless of the notification toggle. The notification text is `messages.friend_offline` (the file's text, written in the server's language) | event | quit the server while at least one online friend is watching, with `notifications.friend_offline: true` | n/a | n/a | internal | brief | SocialListener#onPlayerQuit |
| ultisocial.event.friend-online-notify | Notify every online friend of a joining player, using `NotificationService` if a bean is available, otherwise a plain chat message. The notification text is `messages.friend_online` (the file's text, written in the server's language) | event | join the server while at least one online friend is watching, with `notifications.friend_online: true` | n/a | n/a | internal | brief | SocialListener#onPlayerJoin |

## Lifecycle Hooks

As of UltiTools 6.3.0 the framework's `reloadSelf()` and `unregisterSelf()` are `final` template
methods. This module's former overrides of both only logged a literal English line and never
called `super`, so they were deleted rather than renamed (`UltiKits/UltiSocial#13`): it has no
`onUnregister()` hook and prints no reload or unload line of its own. It does have an `onReload()`
hook, added by `UltiKits/UltiSocial#15`, whose only work is the removed-key check of
`ultisocial.lifecycle.removed-key-warning` and the config-text write of
`ultisocial.lifecycle.legacy-text-defaults` (both also run when the module is enabled).
`ConfigManager#reloadConfigs` re-initialises, in place, the same `SocialConfig` instance the
container injected into `FriendService`, and `FriendCommand`, `FriendListGUI` and `SocialListener`
read it through `FriendService#getConfig()` at call time, so `/ul reload UltiSocial` (or bare
`/ul reload`, which reloads every module) changes what the next command does.
`ultisocial.lifecycle.reload` supersedes `ultisocial.event.module-reload` (retired with
`UltiKits/UltiSocial#13`; its earlier checklist verdict recorded the defect, not this behaviour).

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.lifecycle.reload | `/ul reload UltiSocial` re-reads `config/social.yml` into the running module, so an edited `tp_to_friend.enabled` governs the next `/friend tp` without a restart; the framework logs its own `Module 'UltiSocial' reloaded.` line and this module adds no reload work of its own beyond the removed-key check of `ultisocial.lifecycle.removed-key-warning`, which prints a line only while `config/social.yml` still holds a key this version no longer reads, and the config-text write of `ultisocial.lifecycle.legacy-text-defaults`, which saves `config/social.yml` only when a value changed. Before `UltiKits/UltiSocial#13` the module's reload override replaced the framework's reload and only logged `UltiSocial configuration reloaded!`, so the reload reported success without reloading anything and an edit took effect only after a restart | event | `/ul reload UltiSocial`, or bare `/ul reload` (framework calls `reloadSelf()`, which reloads configuration, refreshes language, runs the `@ConditionalOnConfig` drift check and logs its own per-module line; this module declares no `/friend reload` subcommand) | n/a | n/a | admin | brief | FriendCommand#teleportToFriend |
| ultisocial.lifecycle.removed-key-warning | When the module is enabled and again on every reload of it (`/ul reload` or `/ul reload UltiSocial`), read the operator's own `config/social.yml` and, for each key this version no longer reads that is still in it, log one console WARNING naming the file, the key, what became of the setting and that the key can be deleted. The keys are `notifications.friend_join_world`, removed by UltiKits/UltiSocial#15 (it never had any effect; the notification it described is the feature request UltiKits/UltiSocial#21, and no other setting replaces it), and `messages.player_blocked` and `messages.player_unblocked`, removed by UltiKits/UltiSocial#23 (nothing ever read them; the block and unblock replies come from the language file); the framework writes a declared default only for a missing key and never deletes one, so every server that ran an earlier version still has them. The line comes from the language file, so it follows the `language` setting. A missing or unparseable file produces no warning, and an error inside the check itself is logged as one warning and never stops the module enabling or reloading. The file checked is the one `SocialConfig` binds (its `@ConfigEntity` value), not a second copy of its path | event | module enable (server start, or loading the module) and every reload of it: bare `/ul reload`, which reloads every module, or `/ul reload UltiSocial` | n/a | n/a | admin | brief | UltiSocial#registerSelf, UltiSocial#onReload, RemovedConfigKeys#warnAboutLeftovers |
| ultisocial.lifecycle.legacy-text-defaults | When the module is enabled and again on every reload of it (`UltiSocial#registerSelf`, `#onReload`, both after the framework has read `config/social.yml` and loaded the language — never from a configuration change listener, which the framework fires before it reloads the language): each of `gui_title` and the ten `messages.*` values that is still built-in text — the Chinese default an earlier version shipped, or this jar's English or Chinese text for it (read from the module jar, never from the language files on disk) — and differs from the current text is replaced with this jar's built-in text for the server's language (its `lang/<language>.yml`), and the file is saved once. An untouched value therefore follows a `language` switch in both directions; a value that differs in any way, even by one character, is the operator's and is kept byte for byte; a text that would break the setting's `@NotEmpty` is never written, and a blank value is refused by the framework's `@NotEmpty` check as in every earlier version; a second start with the same language writes nothing. A single-module `/ul reload UltiSocial` does not re-read the framework's `language`, so a changed `language` is picked up on a bare `/ul reload` or a restart. A failed save is logged as one warning from the language file (`log_config_default_save_failed`) naming the file, and the module keeps showing the new text. The text written is this jar's own built-in text for the server's language (the jar's `lang/<language>.yml`), not the extracted language file on disk, so every value the module writes is one it recognises again; these settings are edited in the config file, and an edit of the extracted language file does not change them (earlier versions never read them from the language file either; the text source decision of 2026-09-25) | event | module enable and every reload of it | n/a | n/a | admin | brief | UltiSocial#registerSelf, UltiSocial#onReload, SocialConfig#materializeText, ConfigTextDefaults |

## Scheduled Tasks

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.task.cleanup-expired-requests | Automatically remove expired pending friend requests (older than `request_timeout` seconds) from the in-memory queue, on a fixed 1200-tick (1-minute) period | scheduled | runs automatically every 1200 ticks (1 minute) while the server is up | n/a | n/a | internal | none | FriendService#cleanupExpiredRequests |

## Data Persistence

Both this module's `@Table` entities record a relationship between TWO players, and both are
genuinely written to the framework's `DataOperator` (unlike, e.g., UltiChat's channel membership,
which is in-memory only) — a friendship or a block therefore survives a server restart. Only the
FRIENDSHIP side is visible from EITHER account: it is stored as two rows, one per direction,
created and removed together. A block is stored as exactly ONE row (`blocker -> blocked`), and
`FriendService#getBlacklist` queries only by the blocker's own `player_uuid` — the blocked player
has no symmetric query and cannot see, from their own `/friend blocklist`, that someone has
blocked them; only the blocker's own list ever shows the relationship.

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.storage.friendship-and-blacklist-survive-restart | A friendship is stored as TWO `FriendshipData` rows (one per direction, `@Table("friendships")`), created together by `acceptRequest` and removed together by `removeFriend`, and is visible from EITHER account's own `/friend list` after a restart with no command re-run needed on either side; a block is stored as ONE `BlacklistData` row (`@Table("blacklist")`, one-directional by design — `isBlocked` checks both directions at READ time via two `isBlockedBy` calls, but `getBlacklist` itself only ever returns the blocker's own rows), so it survives a restart but is visible ONLY from the blocker's own `/friend blocklist` — the blocked player has no symmetric view of it at all | persistence | accept a friend request (for the friendship side, visible from both accounts), or block a player (for the blacklist side, visible only from the blocker's own account), then restart the server and check `/friend list`/`/friend blocklist` | n/a | n/a | admin | detailed | FriendshipData#FriendshipData, BlacklistData#BlacklistData, FriendService#getFriends, FriendService#getBlacklist |

## Configuration

Every `@ConfigEntry`-annotated field on this module's one `@ConfigEntity` class (17 keys total,
matching the reconciliation table's own `@ConfigEntry` count of 17 exactly). This module ships NO
`config/social.yml` resource under `src/main/resources` — the file is generated entirely from
these `@ConfigEntry` field defaults the first time the module boots. The framework writes the title
and the ten messages with the Chinese text each shipped with in every earlier version, and the module
rewrites them in the server's language in the same start (`ultisocial.lifecycle.legacy-text-defaults`;
see Conventions, Language).

Two keys, `messages.player_blocked` and `messages.player_unblocked`, were declared but never read:
the block and unblock replies were fixed text, and now come from the language file. They are
removed (`UltiKits/UltiSocial#23`); an operator's file that still holds them gets a warning
(`ultisocial.lifecycle.removed-key-warning`).

Another, `notifications.friend_join_world`, declared a "notify when a friend joins your world"
feature that was never implemented (no handler for a player changing worlds existed anywhere in
this module). It is removed as of `UltiKits/UltiSocial#15` rather than built — the feature is
recorded as a request in `UltiKits/UltiSocial#21`; removing the setting does not reject it. A fresh
`config/social.yml` no longer contains it; an existing file keeps it, because the framework never
deletes a key from an operator's file.

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.config.social.gui_title | The friend list GUI's own title template, `{COUNT}`/`{MAX}` placeholders | config | `config/social.yml: gui_title (default: written in the server's language, from the jar's catalogue key gui_friend_list; under language: en "&6Friend List &7({COUNT}/{MAX})"; the text shown is the file's text)` | n/a | n/a | admin | brief | FriendListGUI#FriendListGUI |
| ultisocial.config.social.max_friends | Maximum number of friends per player (range 1-500, enforced by `@Range`); enforced both when sending a request and when accepting one | config | `config/social.yml: max_friends (default: 50)` | n/a | n/a | admin | brief | FriendService#sendRequest, FriendService#acceptRequest |
| ultisocial.config.social.messages.already_friends | Message shown when the sender tries to friend-request someone already on their friends list | config | `config/social.yml: messages.already_friends (default: written in the server's language, from the jar's catalogue key already_friends; under language: en "&cYou are already friends with {PLAYER}!"; the text shown is the file's text)` | n/a | n/a | admin | brief | FriendService#sendRequest |
| ultisocial.config.social.messages.blocked | Message shown when a friend-request attempt is refused because either party has blocked the other (bidirectional check) | config | `config/social.yml: messages.blocked (default: written in the server's language, from the jar's catalogue key blocked; under language: en "&cCannot perform friend operations with {PLAYER} due to blacklist"; the text shown is the file's text)` | n/a | n/a | admin | brief | FriendService#sendRequest |
| ultisocial.config.social.messages.friend_added | Message shown to BOTH players when a friend request is accepted | config | `config/social.yml: messages.friend_added (default: written in the server's language, from the jar's catalogue key friend_added; under language: en "&aYou and {PLAYER} are now friends!"; the text shown is the file's text)` | n/a | n/a | admin | brief | FriendService#acceptRequest |
| ultisocial.config.social.messages.friend_offline | Notification sent to online friends when a player disconnects | config | `config/social.yml: messages.friend_offline (default: written in the server's language, from the jar's catalogue key friend_offline; under language: en "&7Your friend {PLAYER} has gone offline"; the text shown is the file's text)` | n/a | n/a | admin | brief | SocialListener#onPlayerQuit |
| ultisocial.config.social.messages.friend_online | Notification sent to online friends when a player joins | config | `config/social.yml: messages.friend_online (default: written in the server's language, from the jar's catalogue key friend_online; under language: en "&aYour friend {PLAYER} is now online!"; the text shown is the file's text)` | n/a | n/a | admin | brief | SocialListener#onPlayerJoin |
| ultisocial.config.social.messages.friend_removed | Message shown to the player who removed a friend (the other side receives no message) | config | `config/social.yml: messages.friend_removed (default: written in the server's language, from the jar's catalogue key friend_removed; under language: en "&cYou have removed {PLAYER} from your friends"; the text shown is the file's text)` | n/a | n/a | admin | brief | FriendService#removeFriend |
| ultisocial.config.social.messages.max_friends_reached | Message shown when a friend-request send or accept would exceed `max_friends` | config | `config/social.yml: messages.max_friends_reached (default: written in the server's language, from the jar's catalogue key max_friends_reached; under language: en "&cYou have reached the maximum number of friends!"; the text shown is the file's text)` | n/a | n/a | admin | brief | FriendService#sendRequest, FriendService#acceptRequest |
| ultisocial.config.social.messages.request_denied | Message shown to the DENYING player (the one who ran `/friend deny`, i.e. the original request's receiver) confirming the denial went through — NOT sent to the original sender, who receives no notification of the denial at all | config | `config/social.yml: messages.request_denied (default: written in the server's language, from the jar's catalogue key request_denied; under language: en "&cYou denied {PLAYER}'s friend request"; the text shown is the file's text)` | n/a | n/a | admin | brief | FriendService#denyRequest |
| ultisocial.config.social.messages.request_received | Message shown to the receiver when a friend request arrives | config | `config/social.yml: messages.request_received (default: written in the server's language, from the jar's catalogue key request_received; under language: en "&e{PLAYER} wants to be your friend! Type /friend accept {PLAYER} to accept"; the text shown is the file's text)` | n/a | n/a | admin | brief | FriendService#sendRequest |
| ultisocial.config.social.messages.request_sent | Message shown to the sender when a friend request is successfully queued | config | `config/social.yml: messages.request_sent (default: written in the server's language, from the jar's catalogue key request_sent; under language: en "&aFriend request sent to {PLAYER}!"; the text shown is the file's text)` | n/a | n/a | admin | brief | FriendService#sendRequest |
| ultisocial.config.social.notifications.friend_offline | Enable the friend-offline chat/notification-service message | config | `config/social.yml: notifications.friend_offline (default: true)` | n/a | n/a | admin | brief | SocialListener#onPlayerQuit |
| ultisocial.config.social.notifications.friend_online | Enable the friend-online chat/notification-service message | config | `config/social.yml: notifications.friend_online (default: true)` | n/a | n/a | admin | brief | SocialListener#onPlayerJoin |
| ultisocial.config.social.request_timeout | Friend request expiry, in seconds (range 10-3600, enforced by `@Range`); checked by the 1-minute scheduled sweep, inline on `/friend accept` (a request past this age is treated as `request_expired` even if the sweep has not yet removed it), and inline on `/friend requests`/`getPendingRequests` (an expired request is filtered out of the view before it is shown) — NOT checked by `/friend deny`, which removes a request by sender name unconditionally whether or not it has expired | config | `config/social.yml: request_timeout (default: 60)` | n/a | n/a | admin | brief | FriendService#cleanupExpiredRequests, FriendService#acceptRequest, FriendService#getPendingRequests, FriendRequest#isExpired |
| ultisocial.config.social.tp_to_friend.cooldown | Per-sender cooldown between successful `/friend tp` teleports, in seconds (range 0-3600, enforced by `@Range`) | config | `config/social.yml: tp_to_friend.cooldown (default: 30)` | n/a | n/a | admin | brief | FriendService#canTeleport, FriendService#getRemainingCooldown |
| ultisocial.config.social.tp_to_friend.enabled | Enable the `/friend tp` command and the friend-list GUI's own left-click teleport action entirely | config | `config/social.yml: tp_to_friend.enabled (default: true)` | n/a | n/a | admin | brief | FriendCommand#teleportToFriend, FriendListGUI#createFriendItem |

### Reconciliation note (@ConditionalOnConfig)

The line-start form of the canonical command reports exactly 0 `@ConditionalOnConfig` sites in
this repository — every bean this module registers (`FriendCommand`, `FriendService`,
`SocialListener`) is unconditional at component-scan time. This is the 0-against-0 line the
reconciliation table states rather than omits.

## Language

Two JUnit guards (`UltiSocialLanguageCatalogueTest`, `UltiSocialCjkLiteralScopeTest`) fail the build
when a key is missing from either catalogue, a catalogue key is read by nothing, or Chinese text
appears outside one.

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.i18n.language | All of this module's chat, GUI, command-description and console text in the server's language: `lang/en.yml` under `language: en`, `lang/zh.yml` under `language: zh` | config | framework `config.yml: language` | n/a | both | admin | none | `lang/en.yml`, `lang/zh.yml`, every `i18n(...)` call |
