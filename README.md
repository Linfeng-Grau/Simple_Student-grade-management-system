# 学生成绩管理系统

> **教师按课程录成绩 · 学生查自己的成绩 · 班长看本班排名**
>
> 一个开箱即用的课程设计项目：双击一个 `.cmd` 就能跑起来，数据存在工程内的文件数据库里，
> **不需要安装数据库、不需要配置服务器**。

---

## 一、功能一览

| 题目要求 | 实现情况 |
| --- | --- |
| 用户角色：教师、学生、班长 | 统一登录页，账号自带角色（`TEACHER` / `STUDENT` / `MONITOR`），登录后自动进入对应工作台 |
| 教师按课程录入平时、考试成绩 | `/teacher/courses` 列出本人课程 → `/teacher/course/{cno}` 录入该课学生成绩 |
| 录入时可设置两种成绩比例 | 录入页顶部比例面板：数字框 + 滑块，两项恒和为 100%，保存后对整门课生效 |
| 两种成绩录入后自动生成总评 | 后端保存时按当前比例计算并落库；页面输入过程中也实时预览总评 |
| 学生查询自己的成绩 | `/student/scores` 只显示本人成绩与绩点汇总 |
| 班长查询本人 + 本班所有同学成绩 | `/monitor/scores`（本人）+ `/monitor/ranking`（全班排名） |
| 按每个同学的成绩基点排序 | 排名按**学分加权平均绩点**降序，同分再比加权平均分 |
| 界面友好、操作方便 | 侧边栏工作台、课程卡片、录入进度条、分数徽章、实时计算、未保存提醒、移动端适配 |
| 教师添加学生（姓名 + 学号） | 「学生管理」分两步：先在「登记学生信息」入库，再在**下拉框里勾选**已登记学生（单选 / 多选）加入某门课或「我的全部课程」；新账号初始密码 `114514` |
| 添加时校验学生是否存在 | 下拉选项**只来自库中已登记学生**，不会凭空创建；学号必须 12 位数字，学生只能加入本班课程，已在课程中的自动忽略 |
| 学生首次登录修改密码 | 强制跳「修改密码」页，必须改成 8~20 位「字母 + 数字」组合才能进业务页 |
| 教师删除学生（按课程分别删除） | 移出只影响当前这门课（选课关系 + 该课成绩），其它课程与账号不受影响 |
| 教师另选 / 取消班长 | 班长是班级维度角色，每班最多一人；可直接换人，也可以取消（允许没有班长） |
| 演示数据信息脱敏 | 全部姓名、学号、工号均为虚构；学生为「演示学生甲/乙/丙」，不含任何真实个人信息 |

---

## 二、三步开始使用

### 1. 准备环境

只需要 **JDK 17 及以上**（本项目在 JDK 26 上验证过）和 **Maven 3.6+**。
不需要安装 MySQL / Tomcat，数据库是工程内的 H2 文件库。

### 2. 打包（首次或改过 Java/JSP 代码后）

```powershell
mvn -DskipTests package
```

### 3. 运行

**最省事：双击工程根目录的 `一键开关.cmd`**

| 当前状态 | 双击后 |
| --- | --- |
| 系统正在运行 | **停止**（结束占用 8081 的 java 进程，并等端口真正释放） |
| 系统没在运行 | **启动**，然后浏览器打开 <http://localhost:8081/login> |

窗口里会打印 `[STOP ] / [ OK  ] started -> http://localhost:8081/login` 之类的提示。
可以右键这个 `.cmd` →「发送到 → 桌面快捷方式」，放到桌面当开关用。

也可以手动来：

```powershell
java -jar target\score-web.war
# 浏览器访问 http://localhost:8081/login
```

或者用 Maven 直接跑（改 JSP 后刷新浏览器即可，不必重新打包）：

```powershell
mvn spring-boot:run
```

> **端口**：默认 **8081**（本机因8080被VSCode内嵌浏览器占用，故改为8081）。需要改端口就编辑
> `src/main/resources/application.properties` 里的 `server.port`，或给开关脚本加 `-Port 9090`。
>
> **注意**：不要同时用终端 `java -jar` 和 VS Code `F5` 启动，两者都占 8081，后启动的会失败。

### 4. 在 VS Code 里启动 / 调试（可选）

工程带 `.vscode/launch.json`：

1. 按 **`F5`**（或「运行和调试」里选 `ScoreApplication（启动成绩系统）`）；
2. 停止点调试工具栏的**红色方块**，或按 **`Shift + F5`**；
3. 可以设断点调试录入与计算逻辑。

### 5. 开关脚本的其它用法

底层脚本是 `tools\toggle-app.ps1`：

```powershell
# 只停（没在运行时什么都不做）
powershell -NoProfile -ExecutionPolicy Bypass -File tools\toggle-app.ps1 -Stop

# 启动并顺手打开浏览器
powershell -NoProfile -ExecutionPolicy Bypass -File tools\toggle-app.ps1 -OpenBrowser

# 换端口
powershell -NoProfile -ExecutionPolicy Bypass -File tools\toggle-app.ps1 -Port 9090
```

它按**端口**结束进程，所以无论系统是 `java -jar`、`mvn spring-boot:run` 还是 F5 启动的，这个开关都能停掉。
启动时日志写到 `logs\app.log`（错误写 `logs\app.err.log`）。

---

## 三、演示账号

### 演示学生（密码统一 `123456`，登录页可一键填入）

| 角色 | 学号 | 姓名 |
| --- | --- | --- |
| 学生 | `202421147011` | 演示学生甲 |
| 学生 | `202421147012` | 演示学生乙 |
| 班长 | `202421147013` | 演示学生丙 |

- 三位演示学生都属于「智能系 2401 班」，已选修 2401 班的 4 门课（C101 / C102 / C103 / C201）。
- **班长是启动时从这 3 人中随机选出的**（`DataSeeder` 用 `new Random()`），
  所以不同机器、不同次初始化看到的班长可能不是「演示学生丙」；
  登录页按钮会实时显示当前班长（标注为「班长」），也可以让教师进「学生管理」随时改。
- 想重新随机一次：停应用 → 删 `data` 目录 → 重启。

### 演示教师（密码统一 `123456`）

| 角色 | 工号 | 姓名 | 负责课程 |
| --- | --- | --- | --- |
| 教师 | `T1001` | 张明 | 2401 班 高等数学 / 大学英语 / 程序设计基础，2403 班 前端开发技术 |
| 教师 | `T1002` | 李静 | 2401 班 数据结构，2402 班 线性代数 / 操作系统 / 计算机网络 |
| 教师 | `T1003` | 王强 | 2402 班 Java 程序设计，2403 班 概率论 / 软件工程 / 数据库原理 |

### 教师新建的学生账号

初始密码固定为 `114514`，首次登录会被强制跳到「修改密码」页，
必须改成 8~20 位、同时包含**字母和数字**、且不等于 `114514` 的新密码后才能进入业务页面。

> **隐私说明**：演示数据里所有姓名都是虚构的（演示学生甲/乙/丙、张明/李静/王强），
> 学号与工号也是编造的，
> 教师添加学生时也请使用虚构姓名与学号，不要录入真实学生的个人信息。

---

## 四、界面与使用指南

### 教师：录入成绩

1. 登录后进「我的课程」，每张课程卡片显示班级人数、已录人数、班级平均分、当前比例与录入进度；
2. 点「录入 / 修改成绩」进入录入页：顶部比例面板（数字框 + 滑块，两项恒和为 100%）；
3. 表格逐行填平时/考试分，**总评与等级随输入即时变化**；
4. 底部吸附式保存条显示「已录 x / y、平均、最高、最低、及格率」，有改动时提示「有修改尚未保存」；
5. 保存后整门课按当前比例重算总评；两项都清空再保存 = 撤销该生成绩。

### 教师：学生管理（登记 → 加课 → 移出 / 班长）

侧边栏进「学生管理」，页面自上而下是三步：

**第一步：登记学生信息（唯一的新增入口）**

- 填 **姓名 + 学号 + 所属班级**，点「登记学生」；
- 校验：学号必须是 **12 位数字**；姓名 2~20 个中文/英文字符（可含空格与「·」）；
  同班已有同名同学会给出提示（不阻止，但提醒别重复录入）；
- 登记成功即写入学生表与登录账号（初始密码 `114514`，首次登录强制改密）。

**第二步：把学生加入课程（下拉勾选，支持多选）**

- 点开下拉框，勾选一名（单选）或多名（多选）学生，选项**只来自库中已登记的学生**，并显示
  「姓名 · 学号 · 班级」；支持输入关键字按姓名 / 学号过滤，标题会实时显示「已选 n 人」；
- 选「某一门课」或「我的全部课程」，点「加入课程」一次性批量加入；
- 校验：勾选项必须确实在库中（否则提示「此学生不存在」）；只能加入本班课程
  （选「全部课程」时非本班学生自动跳过并提示跳过几人）；已在课程中的自动忽略。
- 也可以在课程卡片下方的「本班已登记、还没选这门课」里**点学生名字一键加入**。

**第三步：课程名单管理**

每门课卡片列出学生名单，每行三个操作：

- **设为班长**：该生成为本班班长（原班长自动变回普通学生）；
- **取消班长**：该班变为「没有班长」；
- **移出本课**：只把这名学生从当前这门课移出（该课成绩一起删除），
  **其它课程、账号、班长身份都不受影响**。

### 学生：查看成绩

「我的成绩」页顶部四个汇总卡（加权平均绩点 / 加权平均分 / 已修学分 / 已录与不及格），
下方是每门课的平时、考试、比例、总评、绩点与等级明细。

### 班长：查看班级排名

班长除「我的成绩」外多一个「班级排名」：按加权平均绩点降序，前三名有金银铜徽章，
本人所在行高亮并标注「我」。普通学生访问排名页会被拦到「无权访问」。

### 首次登录修改密码

教师新建的学生账号登录后，除改密页外的所有地址都会被重定向到「修改密码」，
弱密码（纯字母、纯数字、含空格、等于初始密码）会当场提示原因；改完自动回到角色首页。

---

## 五、业务规则

**总评**

```
总评 = 平时成绩 × 平时比例 + 考试成绩 × 考试比例
```

比例按百分数保存（如 30 / 70），两项之和必须为 100%；总评保留 1 位小数、四舍五入。

**绩点**

```
总评 ≥ 60：绩点 =（总评 − 50）÷ 10
总评 < 60：绩点 = 0
```

例：总评 87.0 → 绩点 3.70；总评 60 → 1.00；总评 100 → 5.00。

**跨课程汇总**（加权平均分、加权平均绩点都按学分加权）

```
加权绩点 = Σ(课程绩点 × 学分) ÷ Σ学分      （仅统计已录入的课程）
已修学分 = Σ(总评 ≥ 60 的课程学分)
```

**班长排名**：按加权平均绩点降序；绩点相同按加权平均分降序；再相同按学号升序。
尚无任何成绩的同学不参与排名，显示为「—」。

**录入约定**

- 平时、考试都留空 = 该生未录入，保存时会清除其原有成绩（可用于撤销误录）。
- 只填一项 = 填写不完整，整批不保存并提示具体是哪位同学。
- 比例为 0~100 的整数；任一项超出范围或两项之和不是 100，整批不保存。
- 保存失败时页面保留刚填的内容，不需要重新输入。
- 只有**选了这门课的学生**才会出现在录入表格里。

**学生名单（先登记、再加课）**

- 学生属于某门课，由 `course_student(cno, sno)` 记录；同一个学生可以同时出现在多门课里。
- **只有库里已有学生信息才能加入课程**：第二步的选项全部来自已登记学生，不会自动创建；
  新增学生统一在「登记学生信息」里做（姓名 + 学号 + 班级）。
- 加入课程时可以**一次勾选多名学生**批量提交；已经在课程里的会被自动忽略，
  不属于本班课程的学生会被跳过或拒绝（提示具体原因）。
- 合法性校验：学号必须为 12 位数字；姓名 2~20 个中文/英文字符；学生只能加入**本班**课程。
- 移出学生**只对当前这门课生效**：删掉该课的选课关系与成绩，该生仍保留在其它课程，
  账号与班长身份不受影响。题目要求的「不能一次性删除学生在全部课程中的信息」由此保证。

**班长**

- 班长是**班级维度**的角色，每个班最多一名。教师可另选他人（自动替换原班长），
  也可以「取消班长」——即允许某个班没有班长。
- 班长登录后除「我的成绩」外还能看「班级排名」；被取消后立即回到普通学生视图。

**密码**

- 演示账号：`123456`。教师新建学生：`114514`，并置 `must_change_password = true`。
- 强制标记为真时，拦截器会把除 `/password/change` 以外的请求全部重定向到改密页，
  直接拼 URL 也绕不过去。
- 新密码校验：8~20 位、不含空格、同时含字母和数字、不能等于初始密码；
  改成功后清除强制标记并刷新会话中的用户信息，再跳回角色首页。

---

## 六、自检：

### 1. 单元测试

```powershell
mvn test
```

`GradeCalculatorTest` 共 9 项，覆盖：总评四舍五入、59.9 / 60 / 87 / 100 的绩点边界、加权平均、空值与 0~100 校验。

### 2. 端到端脚本

先启动应用，再执行：

```powershell
powershell -ExecutionPolicy Bypass -File tools\verify.ps1
```

脚本覆盖 **47 项**检查，全部通过时输出 `ALL CHECKS PASSED`：

- 未登录访问教师页 → 跳登录
- 登录页列出 3 个可选演示学生，且其中恰好一个被标记为班长
- 教师登录 → 课程页 / 录入页；比例非 100% 被拒绝且不落库
- 录入 80/90、比例 30/70 → 总评 87.0；改比例 50/50 → 重算为 85.0
- 教师访问他人课程 → 400 + 友好错误页
- 学生只能看本人成绩，访问教师页 / 排名页 → 「无权访问」
- 班长可看排名，本人行高亮，绩点序列严格不增
- 教师添加学生（单门课）→ 名单出现该学号
- **学号未登记的学生被拒绝加入课程**（页面给出错误提示、不建学生）
- **不勾选任何学生直接提交被拒绝**；**登记时学号不是 12 位数字被拒绝**
- 先登记学生 → 在**下拉框勾选**后加入课程成功
- **一次勾选两名学生**（多选）→ `enrolled=2`，两名学生都进了名单
- **非本班学生被拒绝加入**
- 新学生用 `114514` 登录 → 被强制跳到 `/password/change`
- 纯字母密码被拒；`abc12345` 通过并跳回学生首页，可正常查成绩
- 按课程移出学生 → 该课名单不再出现，但其其它课程仍在（包含 `C102`、不含 `C101`）
- 设另一个人为班长 → 原班长被替换；取消班长 → 全班没有班长

> 脚本会改数据（新增学生、移出学生、换班长），建议在**刚重置过数据**的环境里跑；
> 想再来一次：停应用 → 删 `data` 目录 → 重启 → 再执行脚本。

### 3. 手工验证要点

1. 教师端在录入框中修改分数，总评与等级立即变化；
2. 拖动比例滑块，总评整列重算；保存后刷新页面，统计条同步更新；
3. 把某位同学两项成绩清空后保存，该生回到「未录入」；
4. 学生端看到的平时 / 考试 / 总评 / 绩点与教师端一致；
5. 班长排名中，绩点高的同学排在前面，前三名有徽章；
6. 教师「学生管理」里**先登记**一名学生（初始密码 114514）→ 再在下拉框里**勾选它**加入课程 →
   用它登录 → 必须改密 → 改完看到本人成绩；
7. 不勾选任何学生直接点「加入课程」→ 应提示先选择学生；
8. 把同一名学生从某一门课移出 → 该课名单少一人，但它仍出现在其它课程里；
9. 把班长改成另一个同学 → 登录页按钮上的「班长」标记跟着变；再取消班长 → 没有任何人被标为班长。

---

## 七、数据保存在哪 / 怎么重置

- 数据是工程根目录下的 H2 文件库：`data\score-db.mv.db`，**重启不丢数据**。
- **恢复初始演示数据**：停应用 → 删除 `data` 目录 → 重启（`DataSeeder` 会重新生成，班长重新随机）。
- 启动时 `DataSeeder` 还会顺带清理旧版演示学生（写死的旧名单），所以升级后不会残留旧数据。
- 想看表内容：启动后访问 <http://localhost:8081/h2-console>，
  JDBC URL 填 `jdbc:h2:file:./data/score-db`，用户名 `sa`，密码留空。
- 注意 `.mv.db` 是 H2 的二进制格式，**不要用文本编辑器打开**。

---

## 八、常见问题

**Q：为什么不用 8080 端口？**
A：本机 8080 被 VS Code 内嵌浏览器（CEF 调试端口）占用。改 `server.port` 即可。

**Q：程序怎么停止？**

| 启动方式 | 停止方式 |
| --- | --- |
| **双击 `一键开关.cmd`**（自己启动的） | **再双击一次**（同一按钮可开可关） |
| 终端 `java -jar` / `mvn spring-boot:run` | 在该终端按 `Ctrl + C`，或关闭该终端 |
| VS Code `F5`（launch.json） | 调试工具栏的红色方块，或 `Shift + F5` |
| Spring Boot Dashboard 的 Run | 同一行的方块 Stop |

找不到终端时，按端口结束进程（只影响占用 8081 的那个 Java 进程）：
`Get-NetTCPConnection -LocalPort 8081 -State Listen | Select-Object -ExpandProperty OwningProcess | ForEach-Object { Stop-Process -Id $_ -Force }`

**Q：启动时报 `Port 8081 was already in use`？**
A：上一次的实例还没退（常见于同时用终端 `java -jar` 和 F5 启动）。先停掉旧进程再启动。
Spring Boot Dashboard 的停止按钮只能停「它自己启动」的进程，终端启动的实例需要 `Ctrl + C` 或按端口结束。

**Q：添加学生时提示「此学生不存在」怎么办？**
A：这是有意为之的严格校验：**只有库里已有学生信息，才能把它加入课程**。
先在「学生管理 → 第一步：登记学生信息」里录入姓名、学号、班级，刷新「第二步」的下拉框后
就能勾选到它（也可以在课程卡片下方点学生名字一键加入）。

**Q：教师添加的学生忘了密码怎么办？**
A：演示项目没有找回密码功能。初始密码固定为 `114514`；若学生已改过密码而忘记，
可以直接删除 `data` 目录重建（会清空成绩），或用 H2 控制台把 `app_user.password` 改回 `114514`
并把 `must_change_password` 置为 `TRUE`。生产环境应由管理员重置密码。

**Q：改 JSP 后需要重新打包吗？**
A：用 `java -jar` 运行时 JSP 在 war 内，需要重新打包（再双击开关即可重启）；
用 `mvn spring-boot:run` 时直接刷新浏览器即可。

**Q：密码为什么是明文？**
A：课程演示项目，账号由 `DataSeeder` 生成并在 README 公开，故未做加盐哈希。
生产环境应改为 BCrypt 等加盐哈希，并增加登录失败次数限制。

**Q：VS Code 里 Java 文件满屏红色波浪线，提示 `is a non-project file` 或
`declared package ... does not match the expected package "main.java..."`？**
A：这是 Java 语言服务器把 `src` 误当成源码根导致的（工作区最初是空目录，导入时还没有
`src/main/java`）。**Maven 构建与运行不受影响**。工程根目录已带 `.project` / `.classpath`，按下面任一步即可恢复：

1. 命令面板执行 `Java: Restart Java Language Server`（最快，不会重载窗口）；
2. 命令面板执行 `Java: Clean Java Language Server Workspace`；
3. 直接 `Developer: Reload Window`。

**Q：为什么不能用 Local Tomcat Launcher / Tomcat 9 部署？**
A：Spring Boot 3/4 使用 `jakarta.servlet`，Tomcat 9 只提供 `javax.servlet`，两者不兼容。
本项目使用内嵌 Tomcat 11，`java -jar` 直接运行。

---

## 九、工程结构

```
Web shangji 3/
├─ pom.xml                          Spring Boot 4.1.1 / war / jasper / H2
├─ README.md
├─ 一键开关.cmd                     双击即开 / 即停（调用 tools\toggle-app.ps1）
├─ tools/toggle-app.ps1             一键开关脚本（8081 在跑就停，没跑就启）
├─ tools/verify.ps1                 端到端验证脚本（37 项检查）
├─ logs/                            用开关启动时的控制台日志（不入库）
├─ data/                            运行时生成的 H2 数据文件（不入库）
├─ .project / .classpath            VS Code Java 工程描述（源码根、m2e 依赖容器）
├─ .vscode/settings.json            UTF-8、Java 工程自动更新
├─ .vscode/launch.json              F5 启动配置（ScoreApplication）
└─ src/
   ├─ main/java/com/example/score/
   │  ├─ ScoreApplication.java       启动类
   │  ├─ common/                     Role、LoginUser、SessionUtil、BizException、PasswordPolicy、
   │  │                              GradeCalculator（总评/绩点唯一口径）、Fmt、HtmlUtil
   │  ├─ config/                     WebConfig、LoginInterceptor、DataSeeder
   │  ├─ entity/                     Clazz、Teacher、Student、Course、CourseStudent、Score、AppUser
   │  ├─ repository/                 Spring Data JPA 接口
   │  ├─ service/                    AuthService、ScoreService、RankingService、StudentAdminService
   │  └─ web/                        Home / Login / PasswordChange / Teacher / Student / Monitor 控制器、
   │                                 GlobalExceptionHandler、view/ 下的 DTO（record）
   ├─ main/resources/
   │  ├─ application.properties
   │  └─ static/css/style.css, static/js/app.js
   ├─ main/webapp/WEB-INF/jsp/
   │  ├─ login.jsp
   │  ├─ common/layout-top.jspf      顶栏 + 侧边栏 + 内容区开头
   │  ├─ common/layout-bottom.jspf
   │  ├─ teacher/courses.jsp, teacher/scores.jsp, teacher/students.jsp
   │  ├─ student/scores.jsp（班长「我的成绩」复用）
   │  ├─ monitor/ranking.jsp
   │  ├─ password/change.jsp（首次登录强制改密）
   │  └─ error/biz.jsp
   └─ test/java/.../GradeCalculatorTest.java
```

---

## 十、数据库设计

```
clazz(class_no PK, class_name)
teacher(tno PK, tname)
student(sno PK, sname, class_no)
course(cno PK, cname, credit, class_no, teacher_no, usual_ratio, exam_ratio)
course_student(id PK, cno, sno)                    -- 选课关系，唯一约束 (cno, sno)
score(id PK, sno, cno, usual_score, exam_score, total_score)   -- 唯一约束 (sno, cno)
app_user(id PK, username UK, password, name, role, ref_no, class_no,
         must_change_password, demo_account)
```

| 表 | 说明 |
| --- | --- |
| `clazz` | 班级 |
| `teacher` | 教师 |
| `student` | 学生，学号即登录账号 |
| `course` | 一门课 = 某教师给某个班上的课；比例保存在课程上 |
| `course_student` | 选课关系：决定录入页、成绩页、删除学生时的范围 |
| `score` | 成绩；`total_score` 在保存时由后端计算 |
| `app_user` | 登录账号；`role` 为 `TEACHER / STUDENT / MONITOR`，`ref_no` 指向工号或学号；`must_change_password` 控制首次改密，`demo_account` 标记登录页可一键填入的演示账号 |

> 表结构由 JPA 自动创建（`ddl-auto=update`）。演示数据由 `DataSeeder` 在「账号表为空」时写入一次，
> 因此重启不会重复插入，也不会覆盖已录入的成绩。

---

## 十一、可以扩展的方向

- 增加课程维护、Excel 导入学生名单（当前学生添加只能单个录入）
- 增加 Excel 导入导出成绩、成绩复核流程
- 引入 Spring Security 做方法级权限与密码加盐哈希
- 增加图表（成绩分布、及格率趋势）
- 增加学期维度，按学期统计 GPA

---

## 十二、技术栈与实现要点

### 技术选型

| 项目 | 选择 | 说明 |
| --- | --- | --- |
| JDK | **26**（本机已有） | Spring Boot 4.1.1 官方支持 Java 17~26；Boot 3.5 只支持到 Java 25，故选用 4.1.1 |
| 框架 | Spring Boot 4.1.1 | starter 名为 `spring-boot-starter-webmvc`（Boot 4 中 `starter-web` 已更名） |
| 视图 | **JSP**（war 打包） | Spring Boot 仅在 **war + 内嵌容器**时支持 JSP，需显式引入 `tomcat-embed-jasper` |
| 容器 | 内嵌 Tomcat 11（Servlet 6.1，`jakarta.servlet.*`） | 直接 `java -jar` 运行，不依赖外部 Tomcat |
| 持久化 | Spring Data JPA + Hibernate 7 + **H2 文件库** | 数据文件在 `data/score-db.mv.db`，重启不丢数据 |
| 前端 | JSP 脚本块 + 原生 CSS/JS | 不引 JSTL/CDN，离线可用；样式集中在 `static/css/style.css` |
| 鉴权 | Session + `HandlerInterceptor` | 未登录跳登录页并记住目标地址，角色不符跳「无权访问」页 |
| 构建 | Maven 3.9 + `maven-war-plugin` | 编译目标 Java 17，产物 `target/score-web.war` |

> 说明：本项目**没有**使用 VS Code 的 Local Tomcat Launcher。该插件内置 Tomcat 9（`javax.servlet`），
> 与 Spring Boot 3/4（`jakarta.servlet`）不兼容，所以使用 Spring Boot 自带的内嵌 Tomcat。

### 关键实现说明

1. **单一成绩口径**：`GradeCalculator` 同时被保存逻辑与单元测试使用；前端 JS 只做即时预览，
   最终总评一律由后端重新计算后落库，避免「页面显示与数据库不一致」。
2. **越权防护**：`LoginInterceptor` 按 URL 前缀校验角色；教师录入页还会校验课程归属
   （`course.teacher_no == 当前工号`），改 URL 也拿不到别人的课程。
3. **整批校验 + 事务**：`ScoreService.saveScores` 在同一个事务里校验全部行，任一处不合法就整批不保存，
   不会出现「只存了一半」的状态。
4. **DTO 隔离**：控制器把实体转换成 `web/view` 下的 record 再交给 JSP，
   配合 `spring.jpa.open-in-view=false`，页面不会触发延迟加载，也不会暴露密码等字段。
5. **JSP 片段编码**：公共片段 `.jspf` 显式声明 `pageEncoding="UTF-8"`。
   Jasper 对静态包含的文件按各自声明解码，缺少声明时中文会变成乱码（本项目已踩过并修复）。
6. **输出转义**：所有用户数据经 `HtmlUtil.esc` 输出，避免 XSS 与页面结构被破坏。
7. **选课关系驱动**：录入页、学生成绩页、学生管理页都以 `course_student` 为唯一口径，
   不再根据学生班级「推算」名单，因此「按课程添加 / 移出学生」不会影响其它课程。
8. **强制改密在拦截器里把关**：`LoginInterceptor` 比角色校验更早执行，
   只要 `must_change_password` 为真，其余 URL 一律重定向到改密页，无法通过直接输地址绕过。

---

**技术栈概要**：Java 17 编译目标 / JDK 26 运行 · Spring Boot 4.1.1 · Spring MVC · Spring Data JPA ·
Hibernate 7 · H2 2.4 · Tomcat 11（内嵌）· JSP · 原生 CSS/JS · Maven