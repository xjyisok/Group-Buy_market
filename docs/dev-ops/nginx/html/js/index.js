// index.js - 现代化轮播 + 倒计时 + 支付弹窗 保留原有逻辑
document.addEventListener("DOMContentLoaded", () => {
    // ----- 基础元素 -----
    const modal = document.getElementById("paymentModal");
    const paymentAmount = document.getElementById("paymentAmount");
    const cancelPayment = document.getElementById("cancelPayment");
    const completePayment = document.getElementById("completePayment");

    // 按钮集合（底部与拼团按钮）
    const buttons = document.querySelectorAll(".group-btn, .action-bar .group-buy, .pill-btn.group-buy, .pill-btn.buy-alone, .action-btn");

    // 事件绑定：点击任意购买按钮显示支付弹窗（需登录检测）
    buttons.forEach(button => {
        button.addEventListener("click", function () {
            // 登录校验（使用 cookie "username"）
            const userId = getCookie("username");
            if (!userId) {
                // 若未登录，跳转到登录页
                window.location.href = "login.html";
                return;
            }
            const price = this.getAttribute("data-price") || this.dataset.price || 0;
            showPaymentModal(price);
        });
    });

    // 显示弹窗
    function showPaymentModal(price) {
        paymentAmount.textContent = `支付金额：¥${price}`;
        modal.setAttribute("aria-hidden", "false");
        // prevent background scroll
        document.body.style.overflow = "hidden";
    }

    function hidePaymentModal() {
        modal.setAttribute("aria-hidden", "true");
        document.body.style.overflow = "";
    }

    cancelPayment.addEventListener("click", hidePaymentModal);
    completePayment.addEventListener("click", () => {
        alert("支付成功！");
        hidePaymentModal();
    });

    // 点击弹窗外部关闭弹窗
    window.addEventListener("click", (e) => {
        if (e.target === modal) hidePaymentModal();
    });

    // cookie 获取
    function getCookie(name) {
        const cookieArr = document.cookie ? document.cookie.split(";") : [];
        for (let i = 0; i < cookieArr.length; i++) {
            let [k, ...v] = cookieArr[i].split("=");
            if (k && k.trim() === name) return decodeURIComponent(v.join("="));
        }
        return null;
    }

    // ---------- 轮播逻辑 ----------
    const swiperWrapper = document.querySelector(".swiper-wrapper");
    const pagination = document.querySelector(".swiper-pagination");
    let slides = Array.from(document.querySelectorAll(".swiper-slide"));
    let currentIndex = 0;
    let autoplayTimer = null;
    const slideCount = slides.length || 1;

    // create dots (accessible)
    function createDots() {
        pagination.innerHTML = "";
        for (let i = 0; i < slideCount; i++) {
            const dot = document.createElement("button");
            dot.className = "swiper-dot" + (i === 0 ? " active" : "");
            dot.setAttribute("aria-label", `第 ${i+1} 页`);
            dot.dataset.index = i;
            dot.addEventListener("click", () => {
                goTo(i);
                restartAutoplay();
            });
            pagination.appendChild(dot);
        }
    }
    createDots();

    function goTo(index) {
        currentIndex = index % slideCount;
        swiperWrapper.style.transform = `translateX(-${currentIndex * 100}%)`;
        slides.forEach((s, i) => s.classList.toggle("active", i === currentIndex));
        document.querySelectorAll(".swiper-dot").forEach((d, i) => d.classList.toggle("active", i === currentIndex));
    }

    function next() {
        goTo((currentIndex + 1) % slideCount);
    }

    function startAutoplay() {
        autoplayTimer = setInterval(next, 3000);
    }
    function stopAutoplay() {
        clearInterval(autoplayTimer);
        autoplayTimer = null;
    }
    function restartAutoplay() {
        stopAutoplay();
        startAutoplay();
    }

    // pause on hover (desktop)
    const swiperContainer = document.querySelector(".swiper-container");
    if (swiperContainer) {
        swiperContainer.addEventListener("mouseenter", stopAutoplay);
        swiperContainer.addEventListener("mouseleave", startAutoplay);
    }

    // initialize
    goTo(0);
    startAutoplay();

    // ---------- 倒计时逻辑 ----------
    class Countdown {
        constructor(element, initialTime) {
            this.element = element;
            this.remaining = Countdown.parseTime(initialTime);
            this.timer = null;
            this.update();
            this.start();
        }
        static parseTime(timeString) {
            // 支持 HH:MM:SS 或者直接秒数
            if (!timeString) return 0;
            if (/^\d+:\d+:\d+$/.test(timeString)) {
                const [h,m,s] = timeString.split(":").map(Number);
                return h*3600 + m*60 + s;
            }
            const asNum = Number(timeString);
            return Number.isFinite(asNum) ? Math.max(0, Math.floor(asNum)) : 0;
        }
        format(seconds) {
            const h = Math.floor(seconds/3600).toString().padStart(2,"0");
            const m = Math.floor((seconds%3600)/60).toString().padStart(2,"0");
            const s = Math.floor(seconds%60).toString().padStart(2,"0");
            return `${h}:${m}:${s}`;
        }
        update() {
            if (this.remaining <= 0) {
                this.element.textContent = "00:00:00";
                this.stop();
                return;
            }
            this.element.textContent = this.format(this.remaining);
        }
        start() {
            if (this.timer) return;
            this.timer = setInterval(() => {
                this.remaining--;
                this.update();
            }, 1000);
        }
        stop() {
            if (this.timer) { clearInterval(this.timer); this.timer = null; }
        }
    }

    // 初始化页面上所有倒计时
    document.querySelectorAll(".countdown").forEach(el => {
        // 保持原有文本若为 HH:MM:SS 则解析，否则假设文本为秒数或默认 0
        const init = el.textContent.trim() || "00:05:49";
        new Countdown(el, init);
    });

    // ---------- 小工具：让某个 Date 加一小时（如果需要） ----------
    window.addOneHour = (date) => {
        if (!(date instanceof Date)) return null;
        return new Date(date.getTime() + 3600_000);
    };

});
