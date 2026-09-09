# UltiSocial — UAT Checklist

This document is the executable companion to `FEATURES.md`: one row per feature stating the
steps to exercise it and the observable truth that proves it works. It is an internal reference
for real-machine verification, not user-facing documentation.

> Batches are dispatched at 60 rows or fewer, and a batch never spans two repositories. There are
> exactly two legitimate exits to `human-uat-pending`: a row needing the pixel layer while the
> real-client harness is not ready, and a row needing personal credentials. Every other row must
> reach `pass`, `fail`, or `blocked`.

## Conventions

- **Columns:** `ID`, `Preconditions`, `Steps`, `Expected`, `Layer`, `Covers`.
- **ID:** cites its `FEATURES.md` ID verbatim. A negative case suffixes the checklist ID only,
  as `.neg-<slug>`.
- **Layer**, copied verbatim from Laojun's own `ultitools-real-client-uat` skill: `protocol`,
  `java-client`, `os-input`, `pixel`, `server`, `human`.
- **Human-authenticated-session rows (D-27b):** this module has no panel-facing surface at all,
  so no row below is affected; the convention is stated here for template consistency.
- **Expected** must name an observable truth and never the words "it works". **This module's own
  text is almost entirely hardcoded Simplified Chinese** (see `FEATURES.md`'s own module-wide
  note) — since this document is English-only (D-02), an Expected cell that names hardcoded
  Chinese text describes it in English ("a Simplified Chinese sentence stating X, see
  `FEATURES.md`'s Source citation for the exact literal") rather than reproducing the CJK
  characters. Only the small minority of rows genuinely driven through `i18n()` (five keys total,
  see `FEATURES.md`) carry the `language: en` precondition and quote literal English text,
  because those five are the only text in this module that setting actually changes.
- **Covers** back-references a Phase 9 GUI-excluded class name; left blank when no such class
  applies. This module owns both of its GUI-excluded classes (`FriendListGUI`, `BlockListGUI`) —
  each is named in exactly one row's Covers cell below.
- A row whose Preconditions name a prior row must appear after that row in file order — asserted
  mechanically: for every row, every checklist ID cited in its Preconditions cell must have a
  strictly smaller line number in this file (sweep class 8, D-27a).
- **Config-per-file rule (D-06):** one checklist row per `@ConfigEntity`-annotated class or per
  shipped yml file. This module has exactly one such file (`config/social.yml`, generated from
  `SocialConfig`'s `@ConfigEntry` defaults — no packaged seed resource exists), so exactly one
  config-per-file row appears below (`ultisocial.config.social-yml`).
- **Nearly every row below needs a second player** (this is a friend/block system — almost
  nothing in it operates on a single account), named explicitly in Preconditions rather than
  left implied.

## Friend Management Commands

| ID | Preconditions | Steps | Expected | Layer | Covers |
|---|---|---|---|---|---|
| ultisocial.friend.add | `Tester1` and `Tester2` online, not already friends, not blocked in either direction, neither at `max_friends` (shipped default 50) | `Tester1` runs `/friend add Tester2` | `Tester1` receives a Simplified Chinese "friend request sent to Tester2" sentence (`SocialConfig#requestSentMessage`'s default text); `Tester2` receives a Simplified Chinese "Tester1 wants to be your friend..." sentence (`SocialConfig#requestReceivedMessage`'s default text) naming the exact `/friend accept Tester1` command | server | |
| ultisocial.friend.add.neg-blocked | `Tester1` and `Tester3` online; `Tester1` has blocked `Tester3` — run `/friend block Tester3` as `Tester1` to reach this state before attempting this row (the checklist row exercising `/friend block` itself appears later in this file; blocking is mechanically simple enough not to need its own row run first) | `Tester3` runs `/friend add Tester1` | `Tester3` receives a Simplified Chinese "cannot perform this action with Tester1 due to a blacklist relationship" sentence (`SocialConfig#blockedMessage`'s default text); no pending request is created | server | |
| ultisocial.friend.add.neg-already-friends | `Tester1` and `Tester2` are already friends — run `/friend add Tester2` as `Tester1`, then `/friend accept Tester1` as `Tester2`, to reach this state before attempting this row (the checklist row exercising `/friend accept` itself, with its own full assertions, appears later in this file) | `Tester1` runs `/friend add Tester2` again | `Tester1` receives a Simplified Chinese "you are already friends with Tester2" sentence (`SocialConfig#alreadyFriendsMessage`'s default text); no duplicate `FriendshipData` row is created | server | |
| ultisocial.friend.add.neg-self | `Tester1` online | `Tester1` runs `/friend add Tester1` | `Tester1` receives a hardcoded (not i18n, not config) Simplified Chinese "cannot add yourself as a friend" sentence, `FriendCommand.java:82` | server | |
| ultisocial.friend.add.neg-offline | `Tester1` online; no player named `NotOnline` currently online | `Tester1` runs `/friend add NotOnline` | `Tester1` receives a hardcoded Simplified Chinese "player NotOnline is not online" sentence, `FriendCommand.java:77` | server | |
| ultisocial.friend.requests | `Tester2` has a pending request from `Tester1` (`ultisocial.friend.add`, run first) | `Tester2` runs `/friend requests` | Chat shows a Simplified Chinese "friend requests" header, then one line naming `Tester1` with an inline hint reading exactly `/friend accept Tester1` — hardcoded, `FriendCommand.java:104-118` | server | |
| ultisocial.friend.requests.neg-empty | `Tester3` has zero pending requests | `Tester3` runs `/friend requests` | Chat shows a hardcoded Simplified Chinese "you have no pending friend requests" sentence and no header/list lines, `FriendCommand.java:109` | server | |
| ultisocial.friend.accept | `Tester2` has a pending request from `Tester1` (`ultisocial.friend.requests`, run first); neither is currently at `max_friends` | `Tester2` runs `/friend accept Tester1` | BOTH `Tester1` and `Tester2` receive a Simplified Chinese "you and X are now friends" sentence (`SocialConfig#friendAddedMessage`'s default text, sent to each side by name); two `FriendshipData` rows now exist (one per direction); the pending request is gone from `Tester2`'s `/friend requests` | server | |
| ultisocial.friend.accept.neg-no-pending | `Tester3` has no pending request from `Tester1` | `Tester3` runs `/friend accept Tester1` | `Tester3` receives the i18n `no_pending_request` message, English text under `language: en`: `No pending friend request from Tester1!` | server | |
| ultisocial.friend.accept.neg-mutual-auto-accept | `Tester1` and `Tester3` online, not already friends, not blocked; `Tester3` has already sent `Tester1` a pending request (run `/friend add Tester1` as `Tester3` first) | `Tester1` runs `/friend add Tester3` (NOT `/friend accept`) | `sendRequest`'s own mutual-request branch fires: the two become friends immediately (both receive the `friend_added`-equivalent config message) WITHOUT `Tester1` ever running `/friend accept` — `Tester3`'s original pending request is consumed, not left in the queue alongside a fresh reverse one | server | |
| ultisocial.friend.list | `Tester1` has exactly one friend, `Tester2` (from `ultisocial.friend.accept`) | `Tester1` runs `/friend list` | Chat shows a Simplified Chinese "friend list (1)" header (count matches), then one line for `Tester2` with a green "online" indicator (since `Tester2` is online) — hardcoded, `FriendCommand.java:56-70` | server | |
| ultisocial.friend.list.neg-empty | `Tester3` has zero friends | `Tester3` runs `/friend list` | Chat shows a hardcoded Simplified Chinese "you have no friends yet, use /friend add <player> to add one" sentence naming the exact command, and no header/list lines, `FriendCommand.java:60` | server | |
| ultisocial.friend.open | `Tester1` holds `ultisocial.use`, with zero or more existing friends | Run bare `/friend` (no arguments) | The `ultisocial.gui.friend-list` GUI opens for `Tester1`; this row proves only that the bare command opens the browser — its own interactions are covered by `ultisocial.gui.friend-list` below | server | FriendListGUI |
| ultisocial.friend.deny | `Tester1` has a pending request from `Tester3` (run `/friend add Tester1` as `Tester3` first, with `Tester1` and `Tester3` NOT already friends and NOT blocked) | `Tester1` runs `/friend deny Tester3` | `Tester1` receives a Simplified Chinese "denied Tester3's friend request" sentence (`SocialConfig#requestDeniedMessage`'s default text); the request is gone from `Tester1`'s `/friend requests`; `Tester1` and `Tester3` are NOT friends afterward | server | |
| ultisocial.friend.deny.neg-no-pending | `Tester2` has no pending request from `Tester3` | `Tester2` runs `/friend deny Tester3` | `Tester2` receives the i18n `no_pending_request` message, English text under `language: en`: `No pending friend request from Tester3!` | server | |
| ultisocial.friend.remove | `Tester1` and `Tester2` are friends (from `ultisocial.friend.accept`) | `Tester1` runs `/friend remove Tester2` | `Tester1` receives a Simplified Chinese "you have removed friend Tester2" sentence (`SocialConfig#friendRemovedMessage`'s default text, sent ONLY to `Tester1`, not to `Tester2`); BOTH `FriendshipData` rows (both directions) are gone from the database; `Tester2`'s own `/friend list` no longer shows `Tester1` either | server | |
| ultisocial.friend.remove.neg-not-friend | `Tester1` and `Tester3` are not friends | `Tester1` runs `/friend remove Tester3` | `Tester1` receives the i18n `not_friend` message, English text under `language: en`: `Tester3 is not your friend!` | server | |
| ultisocial.friend.tp | `Tester1` and `Tester2` are friends, `Tester2` online in a different location; `tp_to_friend.enabled: true` (shipped default); `Tester1` not on cooldown | `Tester1` runs `/friend tp Tester2` | `Tester1` is teleported to `Tester2`'s location; `Tester1` receives a hardcoded Simplified Chinese "teleported to Tester2" sentence, `FriendCommand.java:163` | server | |
| ultisocial.friend.tp.neg-disabled | Same friendship as above; `tp_to_friend.enabled: false` (NOT the shipped default) | `Tester1` runs `/friend tp Tester2` | `Tester1` receives a hardcoded Simplified Chinese "teleport to friend feature is disabled" sentence, `FriendCommand.java:125`; `Tester1`'s location is unchanged | server | |
| ultisocial.friend.tp.neg-cooldown | Same friendship as above, `tp_to_friend.enabled: true`; `Tester1` just successfully teleported via `ultisocial.friend.tp` within the last `tp_to_friend.cooldown` seconds (shipped default 30) | `Tester1` runs `/friend tp Tester2` again immediately | `Tester1` receives a hardcoded Simplified Chinese "teleport on cooldown, wait N seconds" sentence naming a positive remaining-seconds count, `FriendCommand.java:151`; `Tester1`'s location is unchanged | server | |
| ultisocial.friend.msg | `Tester1` and `Tester2` are friends, both online | `Tester1` runs `/friend msg Tester2 hello there`| `Tester2` receives a hardcoded Simplified Chinese private-message line prefixed with a bracketed label, naming `Tester1` and reading `hello there` verbatim; `Tester1` receives a mirrored confirmation line naming `Tester2`, `FriendCommand.java:201,205` | server | |
| ultisocial.friend.msg.neg-not-friend | `Tester1` and `Tester3` are not friends | `Tester1` runs `/friend msg Tester3 hi` | `Tester1` receives a hardcoded Simplified Chinese "Tester3 is not your friend, you can only message friends" sentence; `Tester3` receives nothing, `FriendCommand.java:182` | server | |
| ultisocial.friend.msg.neg-empty | `Tester1` and `Tester2` are friends, both online | `Tester1` runs `/friend msg Tester2 ` followed by only whitespace characters | `Tester1` receives a hardcoded Simplified Chinese "please enter a message to send" sentence naming the exact usage; `Tester2` receives nothing, `FriendCommand.java:196` | server | |
| ultisocial.friend.block | `Tester1` and `Tester3` online, not already blocked in either direction | `Tester1` runs `/friend block Tester3` | `Tester1` receives a hardcoded Simplified Chinese "Tester3 added to blacklist" sentence (NOT `SocialConfig#playerBlockedMessage`, which is declared but never read — `UltiKits/UltiSocial#15`), `FriendCommand.java:224,237`; a new `BlacklistData` row now exists for `Tester1`→`Tester3` | server | |
| ultisocial.friend.block.neg-self | `Tester1` online | `Tester1` runs `/friend block Tester1` | `Tester1` receives a hardcoded Simplified Chinese "cannot block yourself" sentence, `FriendCommand.java:232`; no `BlacklistData` row is created | server | |
| ultisocial.friend.block.neg-already-blocked | `Tester1` has already blocked `Tester3` (from `ultisocial.friend.block`) | `Tester1` runs `/friend block Tester3` again | `Tester1` receives a hardcoded Simplified Chinese "Tester3 is already on your blacklist" sentence, `FriendCommand.java:226,243`; no duplicate `BlacklistData` row is created | server | |
| ultisocial.friend.block.neg-unfriends | `Tester1` and `Tester2` are friends (from `ultisocial.friend.accept`, re-established if `ultisocial.friend.remove` already ran against this pair — use a fresh friendship for this row) | `Tester1` runs `/friend block Tester2` | `Tester1` receives the block-success message AND an additional hardcoded Simplified Chinese "(friendship automatically removed)" line, `FriendCommand.java:240`; both `FriendshipData` rows for this pair are gone, matching `ultisocial.friend.remove`'s own two-sided deletion | server | |
| ultisocial.friend.unblock | `Tester1` has blocked `Tester3` (from `ultisocial.friend.block`) | `Tester1` runs `/friend unblock Tester3` | `Tester1` receives a hardcoded Simplified Chinese "Tester3 removed from blacklist" sentence (NOT `SocialConfig#playerUnblockedMessage`, also declared but never read — `UltiKits/UltiSocial#15`), `FriendCommand.java:250`; the `BlacklistData` row for `Tester1`→`Tester3` is gone; `Tester3` can now send `Tester1` a friend request again | server | |
| ultisocial.friend.unblock.neg-not-blocked | `Tester1` has not blocked `Tester2` | `Tester1` runs `/friend unblock Tester2` | `Tester1` receives a hardcoded Simplified Chinese "Tester2 is not on your blacklist" sentence, `FriendCommand.java:252` | server | |
| ultisocial.friend.blocklist | `Tester1` has at least one blocked player (from `ultisocial.friend.block`, re-block `Tester3` if `ultisocial.friend.unblock` already ran) | `Tester1` runs `/friend blocklist` | The `ultisocial.gui.block-list` GUI opens for `Tester1`; this row proves only that the command opens the browser — its own interactions are covered by `ultisocial.gui.block-list` below | server | BlockListGUI |
| ultisocial.friend.help | `Tester1` online | `Tester1` runs `/friend help` | Chat shows the module's hardcoded (non-i18n, non-config) Simplified Chinese help block, header through the blacklist sub-section, listing all 12 non-help subcommands with a one-line description each — despite `lang/en.yml` shipping a complete unused English translation of the identical block, `FriendCommand.java:264-280` | server | |

## GUI

| ID | Preconditions | Steps | Expected | Layer | Covers |
|---|---|---|---|---|---|
| ultisocial.gui.friend-list | `Tester1` has at least 2 friends, one online (`Tester2`) and one offline; the browser open via `ultisocial.friend.open` | Left-click the online friend's item; separately (re-open), shift+left-click the same item; separately, right-click the online friend's item; separately, right-click the offline friend's item; separately, shift+right-click any friend item; separately, click the pending-requests button (slot 47) when `Tester1` has at least one pending request | Left-click on an online friend teleports exactly as `ultisocial.friend.tp` does; shift+left-click toggles favorite status (a hardcoded Simplified Chinese confirmation appears, and the item's display name gains/loses its star marker on the next `refresh()`); right-click on an online friend closes the inventory and prints the exact `/friend msg <name> <message>` usage hint rather than sending anything; right-click on an OFFLINE friend instead removes them immediately (same effect as `ultisocial.friend.remove`, no confirmation dialogue); shift+right-click always removes regardless of online state; clicking the pending-requests button closes the inventory and runs `/friend requests` as if typed | pixel | FriendListGUI |
| ultisocial.gui.friend-list.neg-no-pending-button | `Tester1` has zero pending requests; the browser open via `ultisocial.friend.open` | Observe slot 47 | No item is placed at slot 47 at all (not merely a disabled-looking one) — the button only exists when `getPendingRequests(...).size() > 0` | pixel | FriendListGUI |
| ultisocial.gui.block-list | `Tester1` has at least 2 blocked players; the browser open via `ultisocial.friend.blocklist` | Left-click a blocked-player item; separately, click the Back button (slot 47) | Left-click unblocks that player exactly as `ultisocial.friend.unblock` does, and the GUI refreshes with one fewer entry; the Back button closes this inventory and opens `ultisocial.gui.friend-list` for the same viewer | pixel | BlockListGUI |
| ultisocial.gui.block-list.neg-empty-state | `Tester3` has zero blocked players; the browser open via `ultisocial.friend.blocklist` | Observe slot 22 | A hardcoded Simplified Chinese "blacklist is empty" item appears at slot 22 — this item appears ONLY when the blacklist is empty, and disappears once at least one entry exists | pixel | BlockListGUI |

## Player Presence Notifications

| ID | Preconditions | Steps | Expected | Layer | Covers |
|---|---|---|---|---|---|
| ultisocial.event.friend-online-notify | `notifications.friend_online: true` (shipped default); `Tester1` and `Tester2` are friends; `Tester1` currently offline, `Tester2` online | `Tester1` joins the server | `Tester2` receives a Simplified Chinese "your friend Tester1 is now online" sentence (`SocialConfig#friendOnlineMessage`'s default text) | server | |
| ultisocial.event.friend-online-notify.neg-disabled | `notifications.friend_online: false` (NOT the shipped default); same friendship as above | `Tester1` joins the server | `Tester2` receives no notification | server | |
| ultisocial.event.friend-offline-notify | `notifications.friend_offline: true` (shipped default); `Tester1` and `Tester2` are friends, both online | `Tester1` disconnects (quit, not kick) | `Tester2` receives a Simplified Chinese "your friend Tester1 has gone offline" sentence (`SocialConfig#friendOfflineMessage`'s default text); `Tester1`'s friend/blacklist caches are cleared regardless of this toggle | server | |
| ultisocial.event.friend-offline-notify.neg-disabled | `notifications.friend_offline: false` (NOT the shipped default); same friendship as above, both online | `Tester1` disconnects | `Tester2` receives no notification; `Tester1`'s caches are still cleared (the cache-clear is unconditional, independent of this toggle) | server | |

## Module Reload

| ID | Preconditions | Steps | Expected | Layer | Covers |
|---|---|---|---|---|---|
| ultisocial.event.module-reload | `Tester1` has at least one friend; note the CURRENT effective value of `max_friends` (shipped default 50 unless already changed) | Hand-edit `plugins/UltiTools/UltiSocial/config/social.yml`'s `max_friends` to a new, distinguishable value (e.g. `2`); run `/ul reload`; then attempt to exceed the OLD limit by befriending enough new players to test whether the new limit applies | `UltiSocial#reloadSelf()` logs a hardcoded "UltiSocial configuration reloaded!" success line; BUT the edited `max_friends` value has NO effect — the OLD limit is still enforced, because `reloadSelf()` never calls `super.reloadSelf()` and so `SocialConfig` is never re-read from disk. A known product defect, `UltiKits/UltiSocial#13` — this row documents the actual (broken) behaviour, not the intended one | server | |

## Scheduled Tasks

| ID | Preconditions | Steps | Expected | Layer | Covers |
|---|---|---|---|---|---|
| ultisocial.task.cleanup-expired-requests | `request_timeout` set to a short, test-friendly value (e.g. 30 seconds, NOT the shipped 60-second default, purely to shorten the wait) BEFORE the request is created; `Tester1` sends `Tester3` a friend request (`ultisocial.friend.add`'s mechanism, any two players not already friends/blocked) | Wait past `request_timeout` seconds, then wait for the next 1200-tick (1-minute) sweep to run, then have `Tester3` run `/friend requests` | The request is gone from `Tester3`'s pending list without `Tester3` ever running `/friend deny` — the scheduled sweep removed it directly from the in-memory queue | server | |

## Data Persistence

| ID | Preconditions | Steps | Expected | Layer | Covers |
|---|---|---|---|---|---|
| ultisocial.storage.friendship-and-blacklist-survive-restart | `Tester1` and `Tester2` are friends (from `ultisocial.friend.accept`); `Tester1` has blocked `Tester3` (from `ultisocial.friend.block`) | Stop the server completely, then start it again, then check BOTH `Tester1`'s and `Tester2`'s own `/friend list`, and `Tester1`'s `/friend blocklist` | `Tester1`'s `/friend list` still shows `Tester2`; `Tester2`'s OWN `/friend list` (run as `Tester2`, not `Tester1`) also still shows `Tester1` — proving the friendship is visible from BOTH accounts, not just the one that originally sent the request; `Tester1`'s `/friend blocklist` still shows `Tester3` | server | |

## Configuration

One row per shipped yml file (D-06's config-per-file rule): `config/social.yml` (20 keys, no
packaged seed resource — generated from `SocialConfig`'s `@ConfigEntry` defaults on first boot).
This row confirms every key is present at its `FEATURES.md`-documented default, then flips one or
more representative keys and observes the behaviour follow — **except the three keys `FEATURES.md`
documents as having no observable effect** (`notifications.friend_join_world`,
`messages.player_blocked`, `messages.player_unblocked`), which this section deliberately does NOT
attempt to exercise for an effect.

| ID | Preconditions | Steps | Expected | Layer | Covers |
|---|---|---|---|---|---|
| ultisocial.config.social-yml | A fresh `plugins/UltiTools/UltiSocial/config/social.yml`, generated by letting the module boot once with no prior file present | Load the file; confirm all 20 keys listed under `FEATURES.md`'s `## Configuration` section are present at their documented defaults; then, in separate configurations so no toggle masks another's effect: (a) set `max_friends: 1` (default 50) and confirm a second friend request is refused with the max-friends message once one friendship already exists; (b) set `tp_to_friend.enabled: false` (default true) and confirm `/friend tp` refuses as `ultisocial.friend.tp.neg-disabled` describes; (c) set `notifications.friend_online: false` (default true) and confirm `ultisocial.event.friend-online-notify` does NOT fire; (d) set `messages.friend_added` to a distinguishable custom string (still containing `{PLAYER}`) and confirm an `/friend accept` success message uses the new text, not the shipped default. Do NOT vary `notifications.friend_join_world`, `messages.player_blocked`, or `messages.player_unblocked` expecting an observable effect — none has one (`UltiKits/UltiSocial#15`) | All 20 keys present at their documented defaults before any change; (a) the second request is refused once the lowered limit is reached; (b) `/friend tp` refuses with the disabled message; (c) no online-notification fires; (d) the accept-success message uses the custom text verbatim (with `{PLAYER}` substituted), not the shipped default | server | |
