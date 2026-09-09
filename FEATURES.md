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
  feature — for every Kind, `config` included: all 20 `config` rows below cite the reading
  member, not `SocialConfig`'s own field declaration.
- **Row order:** by section, then by ID ascending within the section.
- **No manual prose:** no troubleshooting column, no explanatory paragraphs, no draft page text.
  A hazard noticed while reading becomes a negative checklist row, not a note here. Where a
  feature's actual runtime behaviour genuinely diverges from what its own shipped language
  catalogue implies it does (a dead lang key, a config key with no reader), that fact is stated
  here as a plain, sourced observation, with the filed issue number.

### A module-wide fact that shapes almost every row below: this module barely uses its own i18n catalogue

`lang/en.yml`/`lang/zh.yml` each declare 54 keys — an apparently-complete bilingual catalogue
covering every command, both GUIs, and every notification. **Only 5 of the 54 are ever read**
(`grep -rhoE 'i18n\("[a-z_]+"\)' src/main/java/ | sort -u`: `already_sent_request`,
`no_pending_request`, `not_friend`, `request_expired`, `request_not_exist`, all five call sites
inside `FriendService`). Every other player-facing string in this module is either:

- a hardcoded Simplified Chinese string literal directly in `FriendCommand`, `FriendListGUI`,
  `BlockListGUI`, or `SocialListener`'s two click handlers (zero `i18n()` calls exist in any of
  these four files — confirmed by `grep -c "i18n(" <file>` returning 0 for each), which means
  setting `language: en` has **no effect at all** on these — they render in Chinese regardless
  of the framework's language setting; or
- one of `SocialConfig`'s own `@ConfigEntry` string fields (`friendAddedMessage`,
  `requestSentMessage`, `blockedMessage`, …) — genuinely configurable, but shipped with a
  Simplified Chinese default, and NOT re-selected by the `language` setting either (an operator
  who wants English text for these must hand-edit `config/social.yml`).

A known product defect, `UltiKits/UltiSocial#14`, not fixed here per this phase's zero-new-code
rule. Every row below states explicitly which of these three mechanisms drives its own text, so a
reader does not have to open the source to learn whether `language: en` matters for that row.

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
`@ConditionalOnConfig` = 0, `@ConfigEntity` = 1, `@ConfigEntry` = 20, `@Table` = 2 — confirmed by
reading `FriendCommand.java` directly (13 `@CmdMapping` sites at lines 49, 55, 73, 89, 94, 99,
104, 122, 168, 211, 247, 256, 264) and `SocialConfig.java` (20 `@ConfigEntry` sites). The
`find`-based GUI-class count above returns 2, matching Phase 9's own independently-derived
GUI-exclusion register for this module (`FriendListGUI`, `BlockListGUI` — see
`.planning/phases/09-module-ecosystem-readiness-and-test-coverage/gui-exclusions/UltiSocial.md`).
This document's command-row count matches the `@CmdMapping` annotation-site count exactly (13
against 13).

**Reconciliation note — event Kind (3 handler methods against 2 `event`-Kind rows below):** a
deliberate, explained mismatch, the same shape as UltiBackup's own reconciliation note.
`SocialListener` carries 3 `@EventHandler` methods; only `onPlayerJoin` and `onPlayerQuit` are
catalogued as `event`-Kind rows in `## Player Presence Notifications` below. The third
(`onInventoryClick`, which dispatches to two private handlers for the two GUI classes) is the
click-routing implementation *for* this module's `gui`-Kind rows, not an independent
player-visible behaviour of its own — named, by method, in each `gui`-Kind row's own Feature
text in `## GUI` below.

## Friend Management Commands

`FriendCommand` — class-level `@CmdExecutor(alias = {"friend", "friends", "f"}, permission =
"ultisocial.use", description = <Simplified Chinese "friend system">, FriendCommand.java:35)`, `@CmdTarget(PLAYER)`.

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.friend.accept | Accept a pending friend request by the sender's name, creating a BIDIRECTIONAL friendship (one `FriendshipData` row for each side); refuses if the sender's own friend count is already at `max_friends`. All three of this handler's own outcome messages ("no pending request", "request expired", friend-added) route through i18n/config, not hardcoded text — see the row's own Expected for which mechanism drives which branch | command | `/friend accept <player>` | ultisocial.use | player | player | brief | FriendCommand#acceptRequest, FriendService#acceptRequest |
| ultisocial.friend.add | Send a friend request to an online player; auto-accepts immediately if the target had already sent a request to the sender (mutual request collapses into an instant friendship, calling `acceptRequest` internally rather than creating a second pending request). The sender-not-online/self-add checks in `FriendCommand` itself are hardcoded Chinese (no `language: en` effect); `FriendService#sendRequest`'s own outcome messages (blocked/already-friends/max-friends/already-sent/sent/received) are a MIX of `SocialConfig` message fields (Chinese-default, configurable, not `language`-driven) and one i18n key (`already_sent_request`, the only one of these five that respects `language: en`) | command | `/friend add <player>` | ultisocial.use | player | player | brief | FriendCommand#addFriend, FriendService#sendRequest |
| ultisocial.friend.blocklist | Open the blacklist management GUI (`ultisocial.gui.block-list`) for the sender's own blacklist | command | `/friend blocklist` | ultisocial.use | player | player | brief | FriendCommand#openBlockList |
| ultisocial.friend.block | Add a player (online or a known offline player) to the sender's blacklist; automatically removes any existing friendship with that player (both directions). All chat output in this method is hardcoded Chinese (`FriendCommand` itself never calls `i18n()`) — `language: en` has no effect on this row's text | command | `/friend block <player>` | ultisocial.use | player | player | brief | FriendCommand#blockPlayer, FriendService#addToBlacklist |
| ultisocial.friend.deny | Deny a pending friend request by the sender's name, removing it from the pending queue without creating a friendship. Outcome messages route through i18n (`no_pending_request`, `request_not_exist`) or `SocialConfig#requestDeniedMessage` (Chinese-default, config-driven) | command | `/friend deny <player>` | ultisocial.use | player | player | brief | FriendCommand#denyRequest, FriendService#denyRequest |
| ultisocial.friend.help | Print the `/friend` command usage summary, including the blacklist sub-section; entirely hardcoded Chinese chat lines (`FriendCommand#help`/`#handleHelp` never call `i18n()`) despite `lang/en.yml` shipping a complete, unused English translation of every one of these lines (`help_title` through `help_blocklist`) | command | `/friend help` | ultisocial.use | player | player | none | FriendCommand#help |
| ultisocial.friend.list | List the sender's own friends, sorted favorites-first then alphabetically, each with an online/offline indicator; entirely hardcoded Chinese chat output | command | `/friend list` | ultisocial.use | player | player | brief | FriendCommand#listFriends |
| ultisocial.friend.msg | Send a private message to an online friend; refuses a non-friend target, an offline target, or an effectively-empty message (whitespace-only after trimming). Entirely hardcoded Chinese chat output on every branch, including the delivered message's own private-message prefix (a bracketed Simplified Chinese label, `FriendCommand.java:201,205`) — `lang/en.yml`'s `msg_only_friend`/`msg_prefix` keys are declared but unused | command | `/friend msg <player> <message...>` | ultisocial.use | player | player | brief | FriendCommand#sendMessage |
| ultisocial.friend.open | Open the paginated friend list GUI (`ultisocial.gui.friend-list`) for the sender's own friends; this is the module's default (bare-argument) command | command | `/friend` (bare, no arguments) | ultisocial.use | player | player | brief | FriendCommand#openFriendList |
| ultisocial.friend.remove | Remove an existing friendship, removing BOTH sides' `FriendshipData` rows (the sender's own row by ID, and the reverse row via a `WHERE`-matched delete). Outcome messages route through i18n (`not_friend`) or `SocialConfig#friendRemovedMessage` (Chinese-default, config-driven) | command | `/friend remove <player>` | ultisocial.use | player | player | brief | FriendCommand#removeFriend, FriendService#removeFriend |
| ultisocial.friend.requests | List the sender's own pending (received) friend requests, each with an inline hint naming the exact `/friend accept` command to run; entirely hardcoded Chinese chat output | command | `/friend requests` | ultisocial.use | player | player | brief | FriendCommand#viewRequests |
| ultisocial.friend.tp | Teleport the sender to an online friend, subject to `tp_to_friend.enabled` and a per-sender cooldown (`tp_to_friend.cooldown`); uses the framework's `TeleportService` if a bean is available, otherwise falls back to `Player#teleport` directly. Entirely hardcoded Chinese chat output on every branch (disabled/not-friend/offline/cooldown/success) — `lang/en.yml`'s `tp_disabled`/`tp_cooldown`/`tp_success`/`player_not_online` keys are declared but unused | command | `/friend tp <player>` | ultisocial.use | player | player | brief | FriendCommand#teleportToFriend, FriendService#canTeleport |
| ultisocial.friend.unblock | Remove a player from the sender's blacklist by name; entirely hardcoded Chinese chat output on both branches (`lang/en.yml`'s `player_unblocked`/`not_in_blocklist` keys, and `SocialConfig#playerUnblockedMessage`, are all declared but unused — `FriendCommand#unblockPlayer` sends its own hardcoded literal instead of either) | command | `/friend unblock <player>` | ultisocial.use | player | player | brief | FriendCommand#unblockPlayer, FriendService#removeFromBlacklist |

## GUI

Two GUI page classes, neither carrying a page-marking annotation — identified structurally (see
Conventions' own reconciliation note for this Kind). Both are Phase 9's complete GUI-exclusion
register for this module.

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.gui.block-list | Paginated (45-per-page) chest GUI listing the viewer's blacklist, each entry showing the blocked player's skull, block time, and optional reason; left-click unblocks; a Back button (slot 47) returns to `ultisocial.gui.friend-list`. An empty-state item (slot 22) appears only when the blacklist is empty. Click routing lives entirely in `SocialListener#onInventoryClick`/`#handleBlockListClick`, not in this class. Every string (title, lore, click tips) is a hardcoded Chinese literal — `lang/en.yml`'s `gui_blocklist`/`gui_click_unblock` keys are declared but unused | gui | `ultisocial.friend.blocklist`, or the Back button in `ultisocial.gui.friend-list` | n/a | n/a | player | brief | BlockListGUI#updateInventory, SocialListener#onInventoryClick, SocialListener#handleBlockListClick |
| ultisocial.gui.friend-list | Paginated (45-per-page) chest GUI listing the viewer's own friends (favorites first, then alphabetical), each entry a player-head skull showing online/offline status, world and game mode (if online), add time, and per-entry click tips; left-click teleports to an online friend (if enabled and off cooldown), shift+left-click toggles favorite, right-click messages an online friend (prints the exact command to run) or removes an offline one, shift+right-click always removes. A pending-requests button (slot 47) appears only when the viewer has at least one pending request, and opening it runs `/friend requests` via `Player#performCommand`. Click routing lives entirely in `SocialListener#onInventoryClick`/`#handleFriendListClick`, not in this class. The GUI's own title comes from `SocialConfig#guiTitle` (config-driven, Chinese default); every other string (lore, click tips) is hardcoded Chinese — `lang/en.yml`'s `gui_friend_list`/`gui_click_*` keys are declared but unused | gui | `ultisocial.friend.open` | n/a | n/a | player | brief | FriendListGUI#updateInventory, SocialListener#onInventoryClick, SocialListener#handleFriendListClick |

## Player Presence Notifications

`SocialListener` — class-level `@EventListener`. Two of this class's three `@EventHandler`
methods are catalogued here (see the Conventions section's own reconciliation note for the
third, which is `ultisocial.gui.*`'s own click-routing implementation).

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.event.friend-offline-notify | Notify every online friend of a quitting player, using `NotificationService` if a bean is available, otherwise a plain chat message; also unconditionally clears the quitting player's friend/blacklist caches regardless of the notification toggle. The notification text is `SocialConfig#friendOfflineMessage` (Chinese-default, config-driven, not `language`-affected) | event | quit the server while at least one online friend is watching, with `notifications.friend_offline: true` | n/a | n/a | internal | brief | SocialListener#onPlayerQuit |
| ultisocial.event.friend-online-notify | Notify every online friend of a joining player, using `NotificationService` if a bean is available, otherwise a plain chat message. The notification text is `SocialConfig#friendOnlineMessage` (Chinese-default, config-driven, not `language`-affected) | event | join the server while at least one online friend is watching, with `notifications.friend_online: true` | n/a | n/a | internal | brief | SocialListener#onPlayerJoin |

## Module Reload

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.event.module-reload | Intended to reload this module's configuration from disk when the framework reloads it; in reality does nothing but log a success line — `UltiSocial#reloadSelf()` overrides the framework's `reloadSelf()` WITHOUT calling `super.reloadSelf()`, so `SocialConfig` is never re-read and the printed success message describes work that never happened. A known product defect, `UltiKits/UltiSocial#13`, not fixed here per this phase's zero-new-code rule | event | `/ul reload` or `/ul reload UltiSocial` (framework-level; this module declares no `/friend reload` subcommand of its own) | n/a | n/a | admin | brief | UltiSocial#reloadSelf |

## Scheduled Tasks

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.task.cleanup-expired-requests | Automatically remove expired pending friend requests (older than `request_timeout` seconds) from the in-memory queue, on a fixed 1200-tick (1-minute) period | scheduled | runs automatically every 1200 ticks (1 minute) while the server is up | n/a | n/a | internal | none | FriendService#cleanupExpiredRequests |

## Data Persistence

Both this module's `@Table` entities record a relationship between TWO players, and both are
genuinely written to the framework's `DataOperator` (unlike, e.g., UltiChat's channel membership,
which is in-memory only) — a friendship or a block therefore survives a server restart, and is
visible from EITHER account, not just the one that initiated it.

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.storage.friendship-and-blacklist-survive-restart | A friendship is stored as TWO `FriendshipData` rows (one per direction, `@Table("friendships")`), created together by `acceptRequest` and removed together by `removeFriend`; a block is stored as ONE `BlacklistData` row (`@Table("blacklist")`, one-directional by design — `isBlocked` checks both directions at read time via two `isBlockedBy` calls rather than storing two rows). Both survive a full server restart in whichever ORM backend the framework has configured, and both are visible from either side of the relationship: the friend accepting a request sees the friendship from their own `/friend list`, and so does the original sender, without either side re-running any command | persistence | accept a friend request or block a player, then restart the server and check both accounts' `/friend list` or `/friend blocklist` | n/a | n/a | admin | detailed | FriendshipData#FriendshipData, BlacklistData#BlacklistData, FriendService#getFriends, FriendService#getBlacklist |

## Configuration

Every `@ConfigEntry`-annotated field on this module's one `@ConfigEntity` class (20 keys total,
matching the reconciliation table's own `@ConfigEntry` count of 20 exactly). This module ships NO
`config/social.yml` resource under `src/main/resources` — the file is generated entirely from
these `@ConfigEntry` field defaults the first time the module boots.

**Three keys are declared but have no reader anywhere in production code** — a distinct defect
from the language-catalogue one above, since these are genuinely configurable fields with no
consumer at all, not fixed text:

- `notifications.friend_join_world` — no `PlayerChangedWorldEvent` handler exists in this module
  at all; the "notify when a friend joins your world" feature this key implies was never
  implemented. `UltiKits/UltiSocial#15`.
- `messages.player_blocked` / `messages.player_unblocked` — `FriendCommand#blockPlayer`/
  `#unblockPlayer` send their own hardcoded Chinese literals instead of reading
  `SocialConfig#getPlayerBlockedMessage()`/`#getPlayerUnblockedMessage()`; grep confirms zero
  call sites for either getter. `UltiKits/UltiSocial#15`.

| ID | Feature | Kind | How to reach | Permission | Target | Tier | Manual | Source |
|---|---|---|---|---|---|---|---|---|
| ultisocial.config.social.gui_title | The friend list GUI's own title template, `{COUNT}`/`{MAX}` placeholders | config | `config/social.yml: gui_title (default: a Simplified Chinese "friend list" title plus the `{COUNT}`/`{MAX}` placeholders, `SocialConfig.java:49`)` | n/a | n/a | admin | brief | FriendListGUI#FriendListGUI |
| ultisocial.config.social.max_friends | Maximum number of friends per player (range 1-500, enforced by `@Range`); enforced both when sending a request and when accepting one | config | `config/social.yml: max_friends (default: 50)` | n/a | n/a | admin | brief | FriendService#sendRequest, FriendService#acceptRequest |
| ultisocial.config.social.messages.already_friends | Message shown when the sender tries to friend-request someone already on their friends list | config | `config/social.yml: messages.already_friends (default: a Simplified Chinese "you are already friends with {PLAYER}" sentence, `SocialConfig.java:85`)` | n/a | n/a | admin | brief | FriendService#sendRequest |
| ultisocial.config.social.messages.blocked | Message shown when a friend-request attempt is refused because either party has blocked the other (bidirectional check) | config | `config/social.yml: messages.blocked (default: a Simplified Chinese "cannot perform this action with {PLAYER} due to a blacklist relationship" sentence, `SocialConfig.java:89`)` | n/a | n/a | admin | brief | FriendService#sendRequest |
| ultisocial.config.social.messages.friend_added | Message shown to BOTH players when a friend request is accepted | config | `config/social.yml: messages.friend_added (default: a Simplified Chinese "you and {PLAYER} are now friends" sentence, `SocialConfig.java:53`)` | n/a | n/a | admin | brief | FriendService#acceptRequest |
| ultisocial.config.social.messages.friend_offline | Notification sent to online friends when a player disconnects | config | `config/social.yml: messages.friend_offline (default: a Simplified Chinese "your friend {PLAYER} has gone offline" sentence, `SocialConfig.java:65`)` | n/a | n/a | admin | brief | SocialListener#onPlayerQuit |
| ultisocial.config.social.messages.friend_online | Notification sent to online friends when a player joins | config | `config/social.yml: messages.friend_online (default: a Simplified Chinese "your friend {PLAYER} is now online" sentence, `SocialConfig.java:61`)` | n/a | n/a | admin | brief | SocialListener#onPlayerJoin |
| ultisocial.config.social.messages.friend_removed | Message shown to the player who removed a friend (the other side receives no message) | config | `config/social.yml: messages.friend_removed (default: a Simplified Chinese "you have removed friend {PLAYER}" sentence, `SocialConfig.java:57`)` | n/a | n/a | admin | brief | FriendService#removeFriend |
| ultisocial.config.social.messages.max_friends_reached | Message shown when a friend-request send or accept would exceed `max_friends` | config | `config/social.yml: messages.max_friends_reached (default: a Simplified Chinese "your friend count has reached the limit" sentence, `SocialConfig.java:81`)` | n/a | n/a | admin | brief | FriendService#sendRequest, FriendService#acceptRequest |
| ultisocial.config.social.messages.player_blocked | Declared as the message shown when a player is added to the blacklist; never read — `FriendCommand#blockPlayer` sends its own hardcoded Chinese literal instead. Known product defect, `UltiKits/UltiSocial#15` | config | `config/social.yml: messages.player_blocked (default: a Simplified Chinese "{PLAYER} added to blacklist" sentence, `SocialConfig.java:93`, has no effect)` | n/a | n/a | admin | brief | SocialConfig#playerBlockedMessage (declared, never read outside this class) |
| ultisocial.config.social.messages.player_unblocked | Declared as the message shown when a player is removed from the blacklist; never read — `FriendCommand#unblockPlayer` sends its own hardcoded Chinese literal instead. Known product defect, `UltiKits/UltiSocial#15` | config | `config/social.yml: messages.player_unblocked (default: a Simplified Chinese "{PLAYER} removed from blacklist" sentence, `SocialConfig.java:97`, has no effect)` | n/a | n/a | admin | brief | SocialConfig#playerUnblockedMessage (declared, never read outside this class) |
| ultisocial.config.social.messages.request_denied | Message shown to the original sender when their friend request is denied | config | `config/social.yml: messages.request_denied (default: a Simplified Chinese "denied {PLAYER}'s friend request" sentence, `SocialConfig.java:77`)` | n/a | n/a | admin | brief | FriendService#denyRequest |
| ultisocial.config.social.messages.request_received | Message shown to the receiver when a friend request arrives | config | `config/social.yml: messages.request_received (default: a Simplified Chinese "{PLAYER} wants to be your friend, type /friend accept {PLAYER} to accept" sentence, `SocialConfig.java:73`)` | n/a | n/a | admin | brief | FriendService#sendRequest |
| ultisocial.config.social.messages.request_sent | Message shown to the sender when a friend request is successfully queued | config | `config/social.yml: messages.request_sent (default: a Simplified Chinese "friend request sent to {PLAYER}" sentence, `SocialConfig.java:69`)` | n/a | n/a | admin | brief | FriendService#sendRequest |
| ultisocial.config.social.notifications.friend_join_world | Declared as a "notify when a friend joins your world" toggle; never read — no per-world-change handler exists anywhere in this module. Known product defect, `UltiKits/UltiSocial#15` | config | `config/social.yml: notifications.friend_join_world (default: false, has no effect)` | n/a | n/a | admin | brief | SocialConfig#notifyFriendJoinWorld (declared, never read outside this class) |
| ultisocial.config.social.notifications.friend_offline | Enable the friend-offline chat/notification-service message | config | `config/social.yml: notifications.friend_offline (default: true)` | n/a | n/a | admin | brief | SocialListener#onPlayerQuit |
| ultisocial.config.social.notifications.friend_online | Enable the friend-online chat/notification-service message | config | `config/social.yml: notifications.friend_online (default: true)` | n/a | n/a | admin | brief | SocialListener#onPlayerJoin |
| ultisocial.config.social.request_timeout | Friend request expiry, in seconds (range 10-3600, enforced by `@Range`); checked both by the 1-minute scheduled sweep and inline whenever a request is looked up (accept/deny/view) | config | `config/social.yml: request_timeout (default: 60)` | n/a | n/a | admin | brief | FriendService#cleanupExpiredRequests, FriendRequest#isExpired |
| ultisocial.config.social.tp_to_friend.cooldown | Per-sender cooldown between successful `/friend tp` teleports, in seconds (range 0-3600, enforced by `@Range`) | config | `config/social.yml: tp_to_friend.cooldown (default: 30)` | n/a | n/a | admin | brief | FriendService#canTeleport, FriendService#getRemainingCooldown |
| ultisocial.config.social.tp_to_friend.enabled | Enable the `/friend tp` command and the friend-list GUI's own left-click teleport action entirely | config | `config/social.yml: tp_to_friend.enabled (default: true)` | n/a | n/a | admin | brief | FriendCommand#teleportToFriend, FriendListGUI#createFriendItem |

### Reconciliation note (@ConditionalOnConfig)

The line-start form of the canonical command reports exactly 0 `@ConditionalOnConfig` sites in
this repository — every bean this module registers (`FriendCommand`, `FriendService`,
`SocialListener`) is unconditional at component-scan time. This is the 0-against-0 line the
reconciliation table states rather than omits.
