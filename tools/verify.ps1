# 端到端冒烟校验（需先启动应用）。输出全为 ASCII，兼容 PowerShell 5.1 任意代码页。
# 用法：powershell -ExecutionPolicy Bypass -File tools\verify.ps1 [-BaseUrl http://localhost:8081]
param(
    [string]$BaseUrl = "http://localhost:8081"
)

$ErrorActionPreference = "Stop"
$script:failures = 0

function Check {
    param([string]$Name, [bool]$Condition, [string]$Detail = "")
    if ($Condition) {
        Write-Host ("PASS  " + $Name)
    } else {
        Write-Host ("FAIL  " + $Name + "   " + $Detail)
        $script:failures++
    }
}

function New-Session {
    return New-Object Microsoft.PowerShell.Commands.WebRequestSession
}

function Login {
    param([string]$Username, [string]$Password)
    $session = New-Session
    $null = Invoke-WebRequest -Uri "$BaseUrl/login" -WebSession $session -UseBasicParsing
    $response = Invoke-WebRequest -Uri "$BaseUrl/login" -Method Post `
        -Body @{ username = $Username; password = $Password } `
        -WebSession $session -UseBasicParsing
    return @{ Session = $session; Response = $response }
}

# 1. 未登录访问
$anonymous = New-Session
$response = Invoke-WebRequest -Uri "$BaseUrl/teacher/courses" -WebSession $anonymous -UseBasicParsing
Check "anonymous user is redirected to /login" ($response.BaseResponse.ResponseUri.AbsolutePath -eq "/login") `
    $response.BaseResponse.ResponseUri

# 0. 登录页演示学生（其中一人为班长）
$loginPage = (Invoke-WebRequest -Uri "$BaseUrl/login" -WebSession (New-Session) -UseBasicParsing).Content
$demoButtons = [regex]::Matches($loginPage, 'data-role="(monitor|student)"\s+data-fill-user="(\d{12})"')
Check "login page lists 3 selectable demo students" ($demoButtons.Count -eq 3) ("demo buttons: " + $demoButtons.Count)

$monitorSnos = @($demoButtons | Where-Object { $_.Groups[1].Value -eq "monitor" } | ForEach-Object { $_.Groups[2].Value })
Check "exactly one demo student is the class monitor" ($monitorSnos.Count -eq 1) ("monitors: " + ($monitorSnos -join ", "))
$monitorSno = if ($monitorSnos.Count -eq 1) { $monitorSnos[0] } else { "" }
$plainSno = ($demoButtons | Where-Object { $_.Groups[1].Value -eq "student" } | Select-Object -First 1).Groups[2].Value
Check "demo students keep a 12-digit student number" ($demoButtons[0].Groups[2].Value.Length -eq 12) $demoButtons[0].Groups[2].Value

# 2. 教师：课程、录入、比例校验
$teacherLogin = Login "T1001" "123456"
$teacher = $teacherLogin.Session
Check "teacher login lands on /teacher/courses" `
    ($teacherLogin.Response.BaseResponse.ResponseUri.AbsolutePath -eq "/teacher/courses") `
    $teacherLogin.Response.BaseResponse.ResponseUri

$entry = Invoke-WebRequest -Uri "$BaseUrl/teacher/course/C101" -WebSession $teacher -UseBasicParsing
Check "teacher can open the score entry page" ($entry.StatusCode -eq 200) $entry.StatusCode
Check "entry page lists student 202421147011" ($entry.Content -match 'name="usual_202421147011"') "student input not found"

# 比例不是 100% → 不保存
$invalidBody = @{ usualRatio = 30; examRatio = 80; usual_202421147011 = 80; exam_202421147011 = 90 }
$invalid = Invoke-WebRequest -Uri "$BaseUrl/teacher/course/C101" -Method Post -Body $invalidBody -WebSession $teacher -UseBasicParsing
Check "invalid ratio is rejected without saving" `
    (($invalid.BaseResponse.ResponseUri.Query -notmatch "saved=1") -and ($invalid.Content -match "alert-error")) `
    $invalid.BaseResponse.ResponseUri

# 三名演示学生 80 / 90，比例 30 / 70
$snoList = @("202421147011", "202421147012", "202421147013")
$body = @{ usualRatio = 30; examRatio = 70 }
foreach ($sno in $snoList) {
    $body["usual_$sno"] = 80
    $body["exam_$sno"] = 90
}
$saved = Invoke-WebRequest -Uri "$BaseUrl/teacher/course/C101" -Method Post -Body $body -WebSession $teacher -UseBasicParsing
Check "scores saved (30/70)" ($saved.BaseResponse.ResponseUri.Query -match "saved=1") $saved.BaseResponse.ResponseUri
Check "total score is 87.0 for 80/90 with 30/70" ($saved.Content -match '>87\.0<') "87.0 not found"

# 比例改 50 / 50 → 总评重算
$body50 = @{ usualRatio = 50; examRatio = 50 }
foreach ($sno in $snoList) {
    $body50["usual_$sno"] = 80
    $body50["exam_$sno"] = 90
}
$resaved = Invoke-WebRequest -Uri "$BaseUrl/teacher/course/C101" -Method Post -Body $body50 -WebSession $teacher -UseBasicParsing
Check "total is recalculated to 85.0 with 50/50" ($resaved.Content -match '>85\.0<') "85.0 not found after ratio change"

# 恢复原比例
$null = Invoke-WebRequest -Uri "$BaseUrl/teacher/course/C101" -Method Post -Body $body -WebSession $teacher -UseBasicParsing

# 教师不能操作别人的课程（应返回 400）
$blockedStatus = 0
try {
    $null = Invoke-WebRequest -Uri "$BaseUrl/teacher/course/C104" -WebSession $teacher -UseBasicParsing
} catch {
    $webResponse = $_.Exception.Response
    if ($webResponse -and $webResponse.StatusCode) {
        $blockedStatus = [int]$webResponse.StatusCode
    }
}
Check "teacher is blocked from another teacher's course" ($blockedStatus -eq 400) ("status: " + $blockedStatus)

# 3. 学生
$studentLogin = Login $plainSno "123456"
$student = $studentLogin.Session
Check "student login lands on /student/scores" `
    ($studentLogin.Response.BaseResponse.ResponseUri.AbsolutePath -eq "/student/scores") `
    $studentLogin.Response.BaseResponse.ResponseUri

$studentPage = Invoke-WebRequest -Uri "$BaseUrl/student/scores" -WebSession $student -UseBasicParsing
Check "student sees own total score 87.0" ($studentPage.Content -match '>87\.0<') "87.0 not found on student page"
Check "student page shows grade point 3.70" ($studentPage.Content -match '>3\.70<') "3.70 not found"

$denied = Invoke-WebRequest -Uri "$BaseUrl/teacher/courses" -WebSession $student -UseBasicParsing
Check "student cannot open teacher pages" ($denied.BaseResponse.ResponseUri.AbsolutePath -eq "/forbidden") `
    $denied.BaseResponse.ResponseUri

$rankingDenied = Invoke-WebRequest -Uri "$BaseUrl/monitor/ranking" -WebSession $student -UseBasicParsing
Check "plain student cannot open the class ranking" `
    ($rankingDenied.BaseResponse.ResponseUri.AbsolutePath -eq "/forbidden") $rankingDenied.BaseResponse.ResponseUri

# 4. 班长
$monitorLogin = Login $monitorSno "123456"
$monitor = $monitorLogin.Session
Check "monitor login lands on /monitor/scores" `
    ($monitorLogin.Response.BaseResponse.ResponseUri.AbsolutePath -eq "/monitor/scores") `
    $monitorLogin.Response.BaseResponse.ResponseUri

$ranking = Invoke-WebRequest -Uri "$BaseUrl/monitor/ranking" -WebSession $monitor -UseBasicParsing
Check "monitor can open the class ranking" ($ranking.StatusCode -eq 200) $ranking.StatusCode
Check "ranking highlights the monitor row" ($ranking.Content -match 'class="self-tag"') "self tag not found"

$gpas = [regex]::Matches($ranking.Content, 'class="gpa">([0-9]+\.[0-9]+)<') |
    ForEach-Object { [double]$_.Groups[1].Value }
Check "ranking contains all students with scores" ($gpas.Count -ge 3) ("gpa cells: " + $gpas.Count)

$ordered = $true
for ($i = 1; $i -lt $gpas.Count; $i++) {
    if ($gpas[$i] -gt $gpas[$i - 1]) {
        $ordered = $false
    }
}
Check "ranking is sorted by weighted GPA desc" $ordered (($gpas -join ", "))

# 5. 教师学生管理
$studentsPage = Invoke-WebRequest -Uri "$BaseUrl/teacher/students" -WebSession $teacher -UseBasicParsing
Check "teacher can open the student management page" ($studentsPage.StatusCode -eq 200) $studentsPage.StatusCode
Check "student management lists demo student 202421147011" ($studentsPage.Content -match "202421147011") "sno not found"
Check "student management offers a class picker for registration" ($studentsPage.Content -match 'name="classNo"') "class select missing"
Check "student management offers the all-courses option" ($studentsPage.Content -match 'value="ALL"') "ALL option missing"

$newSno = "202421147014"
$secondSno = "202421147015"
$otherClassSno = "202421147016"

Check "student management offers a multi-select student picker" ($studentsPage.Content -match 'name="snos"') "picker missing"

# 1) 登记时学号不是 12 位 → 拒绝
$badFormat = Invoke-WebRequest -Uri "$BaseUrl/teacher/students/register" -Method Post `
    -Body @{ name = "DemoStudentD"; sno = "12345"; classNo = "2401" } `
    -WebSession $teacher -UseBasicParsing
Check "student number must be 12 digits when registering" `
    (($badFormat.BaseResponse.ResponseUri.Query -notmatch "registered=1") -and ($badFormat.Content -match "alert-error")) `
    $badFormat.BaseResponse.ResponseUri

# 2) 未登记学号 → 拒绝加课
$unknown = Invoke-WebRequest -Uri "$BaseUrl/teacher/students/add" -Method Post `
    -Body @{ snos = "202421147099"; target = "C101" } `
    -WebSession $teacher -UseBasicParsing
Check "adding an unregistered student is rejected" `
    (($unknown.BaseResponse.ResponseUri.Query -notmatch "enrolled=") -and ($unknown.Content -match "alert-error")) `
    $unknown.BaseResponse.ResponseUri

# 3) 一个都不勾选 → 拒绝
$emptyPick = Invoke-WebRequest -Uri "$BaseUrl/teacher/students/add" -Method Post `
    -Body @{ target = "C101" } `
    -WebSession $teacher -UseBasicParsing
Check "submitting without selecting any student is rejected" ($emptyPick.Content -match "alert-error") "no error shown"

# 4) 先登记两名学生，才会出现在下拉框里
$registered = Invoke-WebRequest -Uri "$BaseUrl/teacher/students/register" -Method Post `
    -Body @{ name = "DemoStudentD"; sno = $newSno; classNo = "2401" } `
    -WebSession $teacher -UseBasicParsing
Check "teacher can register a new student" ($registered.BaseResponse.ResponseUri.Query -match "registered=1") `
    $registered.BaseResponse.ResponseUri
Check "registered student shows up in the picker" ($registered.Content -match $newSno) "sno not found after register"

$registered2 = Invoke-WebRequest -Uri "$BaseUrl/teacher/students/register" -Method Post `
    -Body @{ name = "DemoStudentE"; sno = $secondSno; classNo = "2401" } `
    -WebSession $teacher -UseBasicParsing
Check "teacher can register a second student" ($registered2.BaseResponse.ResponseUri.Query -match "registered=1") `
    $registered2.BaseResponse.ResponseUri

# 5) 单选：加入 C101
$single = Invoke-WebRequest -Uri "$BaseUrl/teacher/students/add" -Method Post `
    -Body @{ snos = $newSno; target = "C101" } `
    -WebSession $teacher -UseBasicParsing
Check "single-selected student is added to the course" ($single.BaseResponse.ResponseUri.Query -match "enrolled=1") `
    $single.BaseResponse.ResponseUri
$c101AfterAdd = Invoke-WebRequest -Uri "$BaseUrl/teacher/course/C101" -WebSession $teacher -UseBasicParsing
Check "new student appears on the C101 score sheet" ($c101AfterAdd.Content -match "usual_202421147014") "not enrolled in C101"

# 6) 多选：两名学生一次加入 C102
#    PowerShell 5.1 把数组放进 -Body 哈希表不会重复同名 key，所以手工拼表单串
$multiBody = "snos=$newSno&snos=$secondSno&target=C102"
$multi = Invoke-WebRequest -Uri "$BaseUrl/teacher/students/add" -Method Post `
    -Body $multiBody -ContentType "application/x-www-form-urlencoded" `
    -WebSession $teacher -UseBasicParsing
Check "multi-selected students are added at once" ($multi.BaseResponse.ResponseUri.Query -match "enrolled=2") `
    $multi.BaseResponse.ResponseUri

# 7) 非本班学生 → 拒绝
$null = Invoke-WebRequest -Uri "$BaseUrl/teacher/students/register" -Method Post `
    -Body @{ name = "DemoStudentF"; sno = $otherClassSno; classNo = "2402" } `
    -WebSession $teacher -UseBasicParsing
$crossClass = Invoke-WebRequest -Uri "$BaseUrl/teacher/students/add" -Method Post `
    -Body @{ snos = $otherClassSno; target = "C101" } `
    -WebSession $teacher -UseBasicParsing
Check "student from another class is rejected" `
    (($crossClass.BaseResponse.ResponseUri.Query -notmatch "enrolled=") -and ($crossClass.Content -match "alert-error")) `
    $crossClass.BaseResponse.ResponseUri

# 首次登录必须改密
$newLogin = Login $newSno "114514"
Check "new student is forced to /password/change" `
    ($newLogin.Response.BaseResponse.ResponseUri.AbsolutePath -eq "/password/change") `
    $newLogin.Response.BaseResponse.ResponseUri
$newSession = $newLogin.Session

$weak = Invoke-WebRequest -Uri "$BaseUrl/password/change" -Method Post `
    -Body @{ newPassword = "abcdefgh"; confirmPassword = "abcdefgh" } `
    -WebSession $newSession -UseBasicParsing
Check "letters-only password is rejected" ($weak.Content -match "alert-error") "no validation error shown"

$changed = Invoke-WebRequest -Uri "$BaseUrl/password/change" -Method Post `
    -Body @{ newPassword = "abc12345"; confirmPassword = "abc12345" } `
    -WebSession $newSession -UseBasicParsing
Check "letter+digit password is accepted and redirected home" `
    ($changed.BaseResponse.ResponseUri.AbsolutePath -eq "/student/scores") $changed.BaseResponse.ResponseUri
$newScores = Invoke-WebRequest -Uri "$BaseUrl/student/scores" -WebSession $newSession -UseBasicParsing
Check "new student can open /student/scores after changing the password" ($newScores.StatusCode -eq 200) $newScores.StatusCode

# 6. 按课程移出学生
$removed = Invoke-WebRequest -Uri "$BaseUrl/teacher/students/remove" -Method Post `
    -Body @{ cno = "C101"; sno = "202421147013" } -WebSession $teacher -UseBasicParsing
Check "teacher can remove a student from one course" ($removed.BaseResponse.ResponseUri.Query -match "removed=1") $removed.BaseResponse.ResponseUri

$c101 = Invoke-WebRequest -Uri "$BaseUrl/teacher/course/C101" -WebSession $teacher -UseBasicParsing
Check "removed student is gone from C101" (-not ($c101.Content -match "usual_202421147013")) "still listed in C101"

$three = Login "202421147013" "123456"
$threeScores = Invoke-WebRequest -Uri "$BaseUrl/student/scores" -WebSession $three.Session -UseBasicParsing
Check "removal only affects that course, other courses remain" `
    (($threeScores.StatusCode -eq 200) -and ($threeScores.Content -match "C102") -and (-not ($threeScores.Content -match "C101"))) `
    "course list after removal is wrong"

# 7. 班长设置
$promote = Invoke-WebRequest -Uri "$BaseUrl/teacher/students/monitor" -Method Post `
    -Body @{ sno = "202421147011"; action = "set" } -WebSession $teacher -UseBasicParsing
Check "teacher can promote a student to monitor" ($promote.BaseResponse.ResponseUri.Query -match "monitor=1") $promote.BaseResponse.ResponseUri

$afterPromote = (Invoke-WebRequest -Uri "$BaseUrl/login" -WebSession (New-Session) -UseBasicParsing).Content
$promoted = @([regex]::Matches($afterPromote, 'data-role="monitor"\s+data-fill-user="(\d{12})"') | ForEach-Object { $_.Groups[1].Value })
Check "promotion replaces the previous monitor" (($promoted.Count -eq 1) -and ($promoted[0] -eq "202421147011")) ($promoted -join ", ")

$null = Invoke-WebRequest -Uri "$BaseUrl/teacher/students/monitor" -Method Post `
    -Body @{ sno = "202421147011"; action = "clear" } -WebSession $teacher -UseBasicParsing
$afterClear = (Invoke-WebRequest -Uri "$BaseUrl/login" -WebSession (New-Session) -UseBasicParsing).Content
$cleared = [regex]::Matches($afterClear, 'data-role="monitor"')
Check "class can be left without a monitor" ($cleared.Count -eq 0) ("monitors: " + $cleared.Count)

# 汇总
Write-Host ""
if ($script:failures -eq 0) {
    Write-Host "ALL CHECKS PASSED"
    exit 0
} else {
    Write-Host ("FAILED CHECKS: " + $script:failures)
    exit 1
}
