# KeybindInterval
![image](src/main/resources/logo.png)
## 環境
- ```forge 1.20.1```
- サーバー・クライアントの両方に導入必須
## 機能
特定のキーの使用間隔や連続使用時間を制限できるMod

- 設定ファイル```config/keybindinterval.yml```
- 起動時に```config```フォルダ内に```keybindinterval.yml```が自動生成される
- サーバーに参加すると、設定が自動的に適用される
- キーバインドごとに使用間隔 (interval) や最大連続使用時間 (max_time) を設定可能
- 一時的な変更はコマンドで可能だが、設定を保存するには明示的に保存する必要がある

## 導入手順
- ```mods```フォルダに追加
- ```config/keybindinterval.yml``` を編集

## 設定ファイル (```keybindinterval.yml```) のフォーマット
```yaml
path:
  interval: <整数>   # 使用間隔 (tick単位)
  max_time: <整数>   # 最大連続使用時間 (tick単位)
```
| 項目	      | 型	       | 説明                |
|----------|----------|-------------------|
| path     | String   | 設定する項目のパス         |
| interval | 	Number	 | 使用間隔 (tick単位)     |
| max_time | 	Number	 | 最大連続使用時間 (tick単位) |

## コマンド
コマンドでの変更はあくまで一時的なものです。サーバーの再起動後も反映させるには、設定を保存する必要があります。

### プレイヤーの設定を変更
```
/keybindinterval <targets> <path> <category> <value>
```
- targets: プレイヤーのリスト (player, @a, @e, @p など)
- path: 設定するキーのパス
- category: 設定のカテゴリ (interval, max_time)
- value: 設定する値

### コンフィグ設定の変更 (全プレイヤーの設定を変更)
```
/keybindinterval config set <path> <category> <value>
```
### 設定ファイルをリロード (全プレイヤーの設定を変更)
```
/keybindinterval config reload
```
```keybindinterval.yml``` を再読み込みし、全プレイヤーに設定を適用する。
### 現在の設定を取得
```
/keybindinterval config get <path>
```
指定したキー設定の現在の値を表示する。
### 現在の設定を ```config/keybindinterval.yml``` に保存
```
/keybindinterval config save
```
現在の設定を保存し、次回起動時にも適用されるようにする。