# 中国人能飞 ChineseCanFly

Minecraft Java 版 **1.21.11 Fabric** 客户端模组：当游戏语言为 **简体中文（zh_cn）、繁體中文-台灣（zh_tw）、繁體中文-香港（zh_hk）、文言文（lzh）** 之一时，在**未对局域网开放的单人存档**解锁飞行权限——不管是生存、冒险还是其他模式，都可以像创造模式一样**双击空格**起飞。

把语言切换成非中文后会**立即自动收回**飞行权限（创造/旁观模式自带的飞行不受影响）。改语言不用重启游戏，切完即生效。

- 仅客户端模组，只在**未对局域网开放**的单人存档（集成服务端）生效；进入任何多人游戏时都不做任何事。
- 单人存档一旦"对局域网开放"，便视为多人游戏：本模组会收回自己此前授予房主的飞行权限，且不会给房主或加入的玩家授予飞行权限；加入玩家无需安装本模组。
- 由集成服务端授予并同步权限，因此生存模式飞行不会被判定为"非法飞行"。

## 安装使用

1. 安装 [Fabric Loader](https://fabricmc.net/use/)（选择游戏版本 1.21.11）。
2. 把 **Fabric API**（`0.141.4+1.21.11`，[Modrinth 下载](https://modrinth.com/mod/fabric-api/versions?g=1.21.11)）和本模组的 `chinesecanfly-1.0.0.jar` 一起放进 `.minecraft/mods` 文件夹。
3. 启动游戏，语言设为中文，进入任意单人存档，双击空格即可飞行。

## 从源码构建 jar

需要 **JDK 21 或更高**（推荐 [Temurin 21](https://adoptium.net/)）。

- Windows：在工程目录打开命令行，运行 `gradlew.bat build`
- macOS / Linux：`chmod +x gradlew && ./gradlew build`

首次构建会自动下载 Gradle 9.4.1 和 Minecraft 依赖（数百 MB，耐心等待）。
构建产物在 `build/libs/chinesecanfly-1.0.0.jar`（**不要**用带 `-sources` 后缀的那个）。

没有本地环境？把工程推到 GitHub 仓库，`.github/workflows/build.yml` 会让 Actions 自动构建，在 Actions 页面的 Artifacts 里直接下载 jar。

## 自定义

- **语言范围**：改 `src/main/java/com/chinesecanfly/ChineseCanFlyClient.java` 顶部的 `CHINESE_LANGUAGES` 集合。
- **切换语言后保留飞行**：删掉 `onEndClientTick` 中的整个 `else if` 分支即可。
- **版本号 / 包名**：见 `gradle.properties`。

## 原理

每个客户端 tick 读取当前语言；若为中文，则在集成服务端线程把所有在线玩家的 `abilities.allowFlying` 置为 `true` 并向客户端同步能力数据包。切换到非中文时，对非创造/旁观玩家收回该权限并结束飞行状态。

## License

MIT
