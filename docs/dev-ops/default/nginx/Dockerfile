FROM nginx:1.25.1

# 覆盖默认 nginx 静态目录
COPY html /usr/share/nginx/html

# 替换默认 nginx 配置（可选）
COPY default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
