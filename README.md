# UltiSocial - Minecraft 好友系统插件

[![UltiTools-API](https://img.shields.io/badge/UltiTools--API-6.3.0%2B-blue)](https://github.com/UltiKits/UltiTools-Reborn)
[![Paper](https://img.shields.io/badge/Paper-1.21%2B-green)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21%2B-orange)](https://adoptium.net/)

UltiSocial 是一个基于 UltiTools-API 框架开发的 Minecraft 好友系统插件，提供完整的好友管理、私聊、传送和黑名单功能。

## ✨ 功能特性

### 🤝 好友系统

- **好友请求**: 发送、接受、拒绝好友请求
- **好友管理**: 添加、删除好友，支持收藏功能
- **好友列表**: 美观的 GUI 界面，分页显示，区分在线/离线状态
- **上下线通知**: 好友上下线时自动提醒

### 💬 社交功能

- **私聊系统**: 只允许好友之间私聊，保护玩家隐私
- **好友传送**: 一键传送到好友位置，支持冷却时间设置
- **游戏模式显示**: 在好友列表中显示在线好友的游戏模式

### 🚫 黑名单系统
- **双向屏蔽**: 拉黑后双方都无法发送好友请求
- **自动解除**: 拉黑时自动解除已有的好友关系
- **无上限**: 黑名单数量不限制
- **独立管理**: 专属黑名单 GUI 界面

## 📦 安装

1. 下载 UltiTools-API 核心插件，版本需为 6.3.0 或更高。本模块声明 `api-version: 630`，更早的框架会拒绝加载它
2. 将 `UltiSocial.jar` 放入 `plugins/UltiTools/plugins/` 目录
3. 重启服务器以加载模块（`/ul reload` 只重新读取配置，不会加载新的模块 jar）

## 📝 命令

| 命令 | 描述 | 权限 |
|------|------|------|
| `/friend` | 打开好友列表 GUI | `ultisocial.friend` |
| `/friend list` | 文字列出所有好友 | `ultisocial.friend` |
| `/friend add <玩家>` | 发送好友请求 | `ultisocial.friend` |
| `/friend accept <玩家>` | 接受好友请求 | `ultisocial.friend` |
| `/friend deny <玩家>` | 拒绝好友请求 | `ultisocial.friend` |
| `/friend remove <玩家>` | 删除好友 | `ultisocial.friend` |
| `/friend tp <好友>` | 传送到好友位置 | `ultisocial.friend.tp` |
| `/friend msg <好友> <消息>` | 向好友发送私聊 | `ultisocial.friend.msg` |
| `/friend requests` | 查看待处理请求 | `ultisocial.friend` |
| `/friend block <玩家>` | 将玩家加入黑名单 | `ultisocial.friend.block` |
| `/friend unblock <玩家>` | 将玩家移出黑名单 | `ultisocial.friend.block` |
| `/friend blocklist` | 打开黑名单 GUI | `ultisocial.friend.block` |
| `/friend help` | 显示帮助信息 | `ultisocial.friend` |

## 🎨 GUI 界面

### 好友列表 (`/friend`)

- **54格界面**: 45个好友位 + 9个导航按钮
- **分页支持**: 自动分页，支持多页浏览
- **颜色区分**:
  - 🟢 绿色玻璃板 = 在线好友
  - 🔴 红色玻璃板 = 离线好友
  - ⭐ 金块 = 收藏的好友
- **操作方式**:
  - 左键点击：传送到好友（在线时）
  - 右键点击：发送私聊（在线时）/ 删除好友（离线时）
  - Shift+左键：收藏/取消收藏
  - Shift+右键：删除好友

### 黑名单管理 (`/friend blocklist`)

- **独立界面**: 专属黑名单管理 GUI
- **玩家信息**: 显示玩家头颅、拉黑时间、拉黑原因
- **快速操作**: 左键点击解除拉黑
- **页面切换**: 支持与好友列表相互切换

## ⚙️ 配置文件

```yaml
# config/social.yml

# Maximum number of friends per player (1-500) / 每位玩家的最大好友数量（1-500）
max_friends: 50

# Friend request timeout in seconds (10-3600) / 好友请求超时时间（秒，10-3600）
request_timeout: 60

# Notify when a friend comes online / goes offline / 好友上线、下线时通知
notifications:
  friend_online: true
  friend_offline: true

# Teleporting to friends / 传送到好友
tp_to_friend:
  enabled: true
  # Cooldown in seconds (0-3600) / 冷却时间（秒，0-3600）
  cooldown: 30

# The friend-list title and the messages below are written in the server's language at start-up
# while they still hold built-in text; a value you edit is kept.
# 好友列表标题和以下消息在仍为内置文本时，会在启动时按服务器语言写入；你改过的值会保留。
gui_title: "&6好友列表 &7({COUNT}/{MAX})"
messages:
  friend_added: "&a你和 {PLAYER} 成为了好友！"
  friend_removed: "&c你已删除好友 {PLAYER}"
  friend_online: "&a你的好友 {PLAYER} 上线了！"
  friend_offline: "&7你的好友 {PLAYER} 下线了"
  request_sent: "&a已向 {PLAYER} 发送好友请求！"
  request_received: "&e{PLAYER} 想和你成为好友！输入 /friend accept {PLAYER} 接受"
  request_denied: "&c已拒绝 {PLAYER} 的好友请求"
  max_friends_reached: "&c你的好友数量已达上限！"
  already_friends: "&c你已经和 {PLAYER} 是好友了！"
  blocked: "&c无法与 {PLAYER} 进行好友操作，因为存在黑名单关系"
```

## 🗄️ 数据存储

UltiSocial 支持多种数据存储方式（由 UltiTools-API 统一管理）：

- **JSON**: 文件存储，适合小型服务器
- **SQLite**: 本地数据库，推荐中小型服务器
- **MySQL**: 远程数据库，适合大型服务器/BungeeCord 网络

### 数据表结构

#### friends 表
| 字段 | 类型 | 描述 |
|------|------|------|
| id | INT | 主键 |
| player_uuid | VARCHAR | 玩家 UUID |
| friend_uuid | VARCHAR | 好友 UUID |
| friend_name | VARCHAR | 好友名称 |
| favorite | BOOLEAN | 是否收藏 |
| nickname | VARCHAR | 好友备注 |
| created_time | BIGINT | 添加时间 |

#### friend_requests 表
| 字段 | 类型 | 描述 |
|------|------|------|
| id | INT | 主键 |
| sender_uuid | VARCHAR | 发送者 UUID |
| receiver_uuid | VARCHAR | 接收者 UUID |
| sender_name | VARCHAR | 发送者名称 |
| created_time | BIGINT | 发送时间 |
| status | VARCHAR | 请求状态 |

#### blacklist 表

| 字段 | 类型 | 描述 |
|------|------|------|
| id | INT | 主键 |
| player_uuid | VARCHAR | 玩家 UUID |
| blocked_uuid | VARCHAR | 被拉黑者 UUID |
| blocked_name | VARCHAR | 被拉黑者名称 |
| created_time | BIGINT | 拉黑时间 |
| reason | VARCHAR | 拉黑原因 |

## 🔧 开发者 API

### 获取 FriendService

```java
import com.ultikits.ultitools.UltiTools;
import com.ultikits.plugins.social.service.FriendService;

// 从容器获取服务
FriendService friendService = UltiTools.getInstance()
    .getContext()
    .getBean(FriendService.class);
```

### 主要 API 方法

```java
// 好友关系管理
boolean areFriends(UUID player1, UUID player2);
List<FriendData> getFriends(UUID playerUuid);
void addFriend(UUID playerUuid, UUID friendUuid, String friendName);
void removeFriend(UUID playerUuid, UUID friendUuid);

// 好友请求
boolean sendRequest(UUID senderUuid, String senderName, UUID receiverUuid);
void acceptRequest(UUID senderUuid, UUID receiverUuid);
void denyRequest(UUID senderUuid, UUID receiverUuid);
List<FriendRequestData> getPendingRequests(UUID playerUuid);

// 黑名单功能
boolean addToBlacklist(UUID playerUuid, UUID blockedUuid, String blockedName, String reason);
boolean removeFromBlacklist(UUID playerUuid, UUID blockedUuid);
boolean isBlocked(UUID player1, UUID player2); // 双向检查
boolean isBlockedBy(UUID playerUuid, UUID blockerUuid); // 单向检查
List<BlacklistData> getBlacklist(UUID playerUuid);
```

### 事件监听示例

```java
@EventListener
public void onPlayerJoin(PlayerJoinEvent event) {
    FriendService friendService = UltiTools.getInstance()
        .getContext()
        .getBean(FriendService.class);
    
    List<FriendData> friends = friendService.getFriends(event.getPlayer().getUniqueId());
    // 处理好友逻辑...
}
```

## 🔗 与其他模块集成

### UltiMail 邮件集成
当安装 UltiMail 模块时，好友请求可以通过游戏内邮件通知：

```java
// GameMailService 接口由 UltiMail 实现
// 使用 @Service(priority = 100) 优先级覆盖默认的 NoOp 实现
GameMailService mailService = UltiTools.getInstance()
    .getContext()
    .getHighestPriorityBean(GameMailService.class);

if (mailService.isAvailable()) {
    mailService.sendMail(receiverUuid, "系统", "好友请求", 
        senderName + " 想和你成为好友！", null);
}
```

### TeleportService 传送服务

好友传送功能使用框架提供的 `TeleportService`：

```java
TeleportService teleportService = UltiTools.getInstance()
    .getContext()
    .getBean(TeleportService.class);

// 异步传送，带冷却检查
teleportService.teleport(player, targetPlayer.getLocation());
```

## 📋 权限节点

| 权限节点 | 描述 | 默认 |
|----------|------|------|
| `ultisocial.friend` | 基础好友功能 | true |
| `ultisocial.friend.tp` | 好友传送功能 | true |
| `ultisocial.friend.msg` | 好友私聊功能 | true |
| `ultisocial.friend.block` | 黑名单功能 | true |
| `ultisocial.friend.bypass` | 绕过好友数量限制 | op |

## 🌐 多语言支持

语言文件位于 `lang/` 目录：
- `zh.yml` - 简体中文
- `en.yml` - English

### 自定义消息或添加新语言

官方语言文件（`zh.yml`、`en.yml`）归 UltiTools 所有：直接修改的官方文件会在下次启动或模块重载时恢复为本版本自带的内容，修改过的文件保留为 `.bak`。要自定义或翻译：

1. 在同一 `lang/` 目录中，把官方文件复制为以其语言代码加连字符开头的新文件（例如把 `en.yml` 复制为 `en-ja.yml`）
2. 修改或翻译副本中的文本；副本中缺少的条目使用文件名开头那种官方语言（此例为 `en`）的文本
3. 在 `plugins/UltiTools/config.yml` 中设置 `language: en-ja`。这是整个服务器唯一的语言设置，不按玩家客户端区分；没有该文件的其他模块使用各自的官方 `en` 文件

文件名不以官方语言代码加连字符开头时（例如 `ja.yml`），副本仍会被读取，但其中缺少的条目显示英文，并记录一条警告（UltiKits/UltiTools-Reborn#616）。

## 📊 变更日志

### v1.1.0 (当前版本) - 2026-02-12

#### UltiTools-API v6.2.0 迁移

- 🚀 **Query DSL**: 数据库查询全面升级到新的 Query DSL API
  - 替换所有 `WhereCondition.builder()` 为 `dataOperator.query().where().eq()`
  - 更流畅的链式调用，更易读的代码
  - 自动优化的 SQL 生成
- 🚀 **@Scheduled 定时任务**: 使用声明式定时任务替代手动 `runTaskTimer`
  - 好友请求过期清理改为 `@Scheduled(period = 1200, async = true)`
  - 自动生命周期管理，无需手动取消
- 🚀 **配置验证**: 添加 `@Range` 和 `@NotEmpty` 注解
  - `max_friends`: 1-500 范围验证
  - `request_timeout`: 10-3600 秒范围验证
  - `tp_to_friend.cooldown`: 0-3600 秒范围验证
  - 所有消息字段添加 `@NotEmpty` 验证
- 🚀 **现代基类**: 迁移到新的基类
  - `FriendshipData` / `BlacklistData`: `AbstractDataEntity` → `BaseDataEntity<String>`
  - `FriendCommand`: `AbstractCommandExecutor` → `BaseCommandExecutor`
- 📝 **代码质量**: 移除过时导入，优化代码结构

#### 功能特性

- ✨ 新增黑名单系统
  - 双向屏蔽功能
  - 独立黑名单 GUI
  - 拉黑时自动解除好友
- ✨ 新增好友传送功能（使用框架 TeleportService）
- ✨ 新增好友私聊功能
- ✨ 好友列表显示游戏模式
- 🔧 优化服务优先级机制（@Service priority）
- 🔧 重构命令系统，支持 Tab 补全
- 🔧 完善 i18n 多语言支持

### v1.0.0
- 🎉 初始版本
- 好友请求系统
- 好友列表 GUI
- 收藏功能
- 上下线通知

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 🔗 相关链接

- [UltiTools-API 文档](https://docs.ultikits.com)
- [GitHub 仓库](https://github.com/UltiKits/UltiTools-Reborn)
- [问题反馈](https://github.com/UltiKits/UltiTools-Reborn/issues)
