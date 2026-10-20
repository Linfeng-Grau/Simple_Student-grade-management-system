/* 前端交互：侧栏、演示账号填充、学生多选下拉、成绩录入 */
(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        initSidebar();
        initDemoAccounts();
        initScoreForm();
        initStudentPicker();
    });

    /* 移动端侧栏 */
    function initSidebar() {
        var toggle = document.getElementById("menuToggle");
        if (!toggle) {
            return;
        }
        toggle.addEventListener("click", function (event) {
            event.stopPropagation();
            document.body.classList.toggle("sidebar-open");
        });
        document.addEventListener("click", function (event) {
            if (!document.body.classList.contains("sidebar-open")) {
                return;
            }
            if (event.target.closest(".sidebar") || event.target.closest("#menuToggle")) {
                return;
            }
            document.body.classList.remove("sidebar-open");
        });
    }

    /* 登录页演示账号填充 */
    function initDemoAccounts() {
        var buttons = document.querySelectorAll("[data-fill-user]");
        if (!buttons.length) {
            return;
        }
        buttons.forEach(function (button) {
            button.addEventListener("click", function () {
                var username = document.getElementById("username");
                var password = document.getElementById("password");
                if (username) {
                    username.value = button.getAttribute("data-fill-user") || "";
                }
                if (password) {
                    password.value = button.getAttribute("data-fill-pass") || "123456";
                }
                if (username) {
                    username.focus();
                }
            });
        });
    }

    /* 学生多选下拉：计数与过滤 */
    function initStudentPicker() {
        var picker = document.getElementById("picker");
        if (!picker) {
            return;
        }
        var summary = document.getElementById("pickerSummary");
        var filter = document.getElementById("pickerFilter");
        var total = picker.querySelectorAll(".picker-item").length;

        function refresh() {
            if (!summary) {
                return;
            }
            var checked = picker.querySelectorAll("input[type='checkbox']:checked").length;
            summary.textContent = checked === 0
                ? "点击选择学生（共 " + total + " 人可选）"
                : "已选 " + checked + " 人（共 " + total + " 人可选）";
        }

        picker.addEventListener("change", refresh);

        if (filter) {
            filter.addEventListener("click", function (event) {
                event.stopPropagation();
            });
            filter.addEventListener("input", function () {
                var keyword = filter.value.trim().toLowerCase();
                picker.querySelectorAll(".picker-item").forEach(function (item) {
                    var key = (item.getAttribute("data-key") || "").toLowerCase();
                    item.hidden = keyword !== "" && key.indexOf(keyword) === -1;
                });
            });
        }

        // 点面板外部时自动收起
        document.addEventListener("click", function (event) {
            if (picker.open && !picker.contains(event.target)) {
                picker.open = false;
            }
        });

        refresh();
    }

    /* 成绩录入：比例联动、总评实时计算、提交校验 */
    function initScoreForm() {
        var form = document.getElementById("scoreForm");
        if (!form) {
            return;
        }
        var usualRatio = document.getElementById("usualRatio");
        var examRatio = document.getElementById("examRatio");
        var usualRange = document.getElementById("usualRange");
        if (!usualRatio || !examRatio) {
            return;
        }

        var rows = Array.prototype.slice.call(document.querySelectorAll("tr[data-sno]"));

        function clampRatio(value) {
            var parsed = parseInt(value, 10);
            if (isNaN(parsed)) {
                return null;
            }
            return Math.max(0, Math.min(100, parsed));
        }

        function markDirty() {
            document.body.classList.add("has-dirty");
        }

        function setRatio(usual) {
            if (usual === null) {
                return;
            }
            usualRatio.value = usual;
            examRatio.value = 100 - usual;
            if (usualRange) {
                usualRange.value = usual;
            }
        }

        function updateRows() {
            var ratio = clampRatio(usualRatio.value);
            if (ratio === null) {
                return;
            }
            var filled = 0;

            rows.forEach(function (row) {
                var usualInput = row.querySelector(".js-usual");
                var examInput = row.querySelector(".js-exam");
                var totalCell = row.querySelector(".js-total");
                var levelCell = row.querySelector(".js-level");
                if (!usualInput || !examInput || !totalCell) {
                    return;
                }

                var usualRaw = usualInput.value.trim();
                var examRaw = examInput.value.trim();
                var usual = usualRaw === "" ? NaN : parseFloat(usualRaw);
                var exam = examRaw === "" ? NaN : parseFloat(examRaw);

                usualInput.classList.toggle("invalid", usualRaw !== "" && (isNaN(usual) || usual < 0 || usual > 100));
                examInput.classList.toggle("invalid", examRaw !== "" && (isNaN(exam) || exam < 0 || exam > 100));

                if (isNaN(usual) || isNaN(exam)) {
                    totalCell.textContent = "—";
                    if (levelCell) {
                        levelCell.innerHTML = '<span class="score-pill none">未录入</span>';
                    }
                    return;
                }

                filled++;
                var raw = (usual * ratio + exam * (100 - ratio)) / 100;
                var total = Math.round(raw * 10) / 10;
                totalCell.textContent = total.toFixed(1);

                if (levelCell) {
                    var level = total >= 80 ? "good" : (total >= 60 ? "pass" : "fail");
                    var text = total >= 80 ? "优秀" : (total >= 60 ? "及格" : "不及格");
                    levelCell.innerHTML = '<span class="score-pill ' + level + '">' + text + "</span>";
                }
            });

            var filledLabel = document.getElementById("filledCount");
            if (filledLabel) {
                filledLabel.textContent = filled + " / " + rows.length;
            }
        }

        usualRatio.addEventListener("input", function () {
            var usual = clampRatio(usualRatio.value);
            if (usual !== null) {
                if (usualRange) {
                    usualRange.value = usual;
                }
                examRatio.value = 100 - usual;
                updateRows();
            }
            markDirty();
        });

        examRatio.addEventListener("input", function () {
            var exam = clampRatio(examRatio.value);
            if (exam !== null) {
                usualRatio.value = 100 - exam;
                if (usualRange) {
                    usualRange.value = usualRatio.value;
                }
                updateRows();
            }
            markDirty();
        });

        if (usualRange) {
            usualRange.addEventListener("input", function () {
                setRatio(clampRatio(usualRange.value));
                updateRows();
                markDirty();
            });
        }

        form.addEventListener("input", function (event) {
            if (event.target.classList.contains("js-usual") || event.target.classList.contains("js-exam")) {
                updateRows();
                markDirty();
            }
        });

        form.addEventListener("submit", function (event) {
            var message = validate();
            if (message) {
                event.preventDefault();
                showError(message);
                return;
            }
            document.body.classList.remove("has-dirty");
        });

        function validate() {
            var usual = clampRatio(usualRatio.value);
            var exam = clampRatio(examRatio.value);
            if (usual === null || exam === null) {
                return "成绩比例需要填写 0~100 的整数。";
            }
            if (usual + exam !== 100) {
                return "平时成绩比例与考试成绩比例之和必须等于 100%。";
            }

            var problems = [];
            rows.forEach(function (row) {
                var usualInput = row.querySelector(".js-usual");
                var examInput = row.querySelector(".js-exam");
                var name = row.getAttribute("data-name") || row.getAttribute("data-sno");
                var usualRaw = usualInput.value.trim();
                var examRaw = examInput.value.trim();

                if (usualRaw === "" && examRaw === "") {
                    return;
                }
                if (usualRaw === "" || examRaw === "") {
                    problems.push(name + "：平时与考试成绩需同时填写，或都留空");
                    return;
                }
                var usual = parseFloat(usualRaw);
                var exam = parseFloat(examRaw);
                if (isNaN(usual) || usual < 0 || usual > 100) {
                    problems.push(name + "：平时成绩需在 0~100 之间");
                }
                if (isNaN(exam) || exam < 0 || exam > 100) {
                    problems.push(name + "：考试成绩需在 0~100 之间");
                }
            });

            if (problems.length) {
                var shown = problems.slice(0, 5).join("；");
                return shown + (problems.length > 5 ? "；…… 共 " + problems.length + " 处问题" : "");
            }
            return "";
        }

        function showError(message) {
            var box = document.getElementById("clientError");
            if (!box) {
                window.alert(message);
                return;
            }
            box.innerHTML = '<span class="alert-icon">!</span><div>' + escapeHtml(message) + "</div>";
            box.hidden = false;
            box.scrollIntoView({ behavior: "smooth", block: "center" });
        }

        window.addEventListener("beforeunload", function (event) {
            if (!document.body.classList.contains("has-dirty")) {
                return;
            }
            event.preventDefault();
            event.returnValue = "";
        });

        updateRows();
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }
})();
