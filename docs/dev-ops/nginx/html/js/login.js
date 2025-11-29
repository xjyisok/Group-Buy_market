document.addEventListener("DOMContentLoaded", () => {
  const loginForm = document.getElementById("loginForm");
  const errorMessage = document.getElementById("errorMessage");

  loginForm.addEventListener("submit", (e) => {
    e.preventDefault();

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    // 你可以替换为实际后端校验
    if (!username || !password) {
      errorMessage.textContent = "请输入用户名和密码";
      errorMessage.style.display = "block";
      return;
    }

    errorMessage.style.display = "none";

    // Cookie 有效期 1 天
    const expirationDate = new Date();
    expirationDate.setDate(expirationDate.getDate() + 1);

    document.cookie = `username=${username}; expires=${expirationDate.toUTCString()}; path=/`;

    // 跳转首页
    window.location.href = "index.html";
  });
});
