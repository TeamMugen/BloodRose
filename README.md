# BloodRose

## What this plugin?
Minecraft 1.20.4で動作するPvP試合進行プラグインです。
現在４つのゲームがプラグインの機能または構想として存在します。
- FFA
- TDM
- SD
- DOM

これらの試合進行に必要ないくつかの処理を内包しています。
- マップ登録
- 参加処理
- 試合処理

また、依存プラグイン導入で使用可能になる機能もあります。
- 報酬付与

## ⚠Alerts
試合進行中に`/rose reload`を使用しないでください。
試合進行が不可となり鯖を再起動する必要がでてきます。

## Commands
Basic command is `/bloodrose`.
The command's aliase is `/rose`.

The command has some subcommands.
The table below tells you how to use the subcommands.

### Subcommands
```
<>: need
[]: option
```
| subcommand | sub-subcommands | function |
|:---:|:---|:---|
|**`reload`**||プラグインの設定ファイルを読み込み|
|**`team`**|||
|**`help`**|||
|**`gametoggle`**|||
|**`chat`**|||
|**`end <mode> <arena>`**|||
|**`join <mode> <arena>`**|||
|**`cancel <player>`**|||
|**`skip [<mode>] [<arena>]`**|||
|**`status [<player>]`**|||
|**`arena`**|`list`||
||`status`||
||`remove <mode> <arena>`||
||`check <mode> <arena>`||
|**`spot`**|`confirm`||
||`cancel`||
||`add <mode> <arena> <spot>`||
||`del <mode> <arena> <spot>`|スポーンポイントを削除|
||`tp <mode> <arena> <spot>`|スポーンポイントへ移動|
||`list <mode> <arena>`|スポーンポイントを一覧表示|
|**`spawn`**|`add <mode> <arena>`|現在地にスポーンポイントを作成|
||`global`||
||`respawn`||
||`del <mode> <arena> <index>`|スポーンポイントを削除|
||`tp <mode> <arena> <index>`|スポーンポイントへ移動|
||`list <mode> <arena>`|スポーンポイントを一覧表示|

## TODO
- [x] プラグインを作る
- [ ] FFAを作る
- [ ] TDMを作る
- [ ] SDを作る
- [ ] DOMを作る
