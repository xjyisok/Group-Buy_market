// 订单明细页面JavaScript
class OrderListManager {
    constructor() {
        this.userId = AppUtils.getUserIdFromUrl(); // 从公共工具获取用户ID
        this.lastId = null;
        this.pageSize = 10;
        this.hasMore = true;
        this.loading = false;
        this.currentRefundOrderId = null;
        
        this.init();
    }
    
    init() {
        this.bindEvents();
        this.displayUserId();
        this.loadOrderList();
    }
    
    bindEvents() {
        // 加载更多按钮事件
        document.getElementById('loadMoreBtn').addEventListener('click', () => {
            this.loadOrderList();
        });
        
        // 退单弹窗事件
        document.getElementById('cancelRefund').addEventListener('click', () => {
            this.hideRefundModal();
        });
        
        document.getElementById('confirmRefund').addEventListener('click', () => {
            this.processRefund();
        });
        
        // 点击弹窗外部关闭
        document.getElementById('refundModal').addEventListener('click', (e) => {
            if (e.target.id === 'refundModal') {
                this.hideRefundModal();
            }
        });
    }
    
    displayUserId() {
        const userIdElement = document.getElementById('userIdDisplay');
        if (userIdElement && this.userId) {
            userIdElement.textContent = `用户ID: ${AppUtils.obfuscateUserId(this.userId)}`;
        }
    }
    
    async loadOrderList() {
        if (this.loading || !this.hasMore) return;
        
        this.loading = true;
        this.showLoading();
        
        try {
            const requestData = {
                userId: this.userId,
                lastId: this.lastId,
                pageSize: this.pageSize
            };
            
            // 调用后端API
            const response = await fetch(AppConfig.sPayMallUrl + '/api/v1/alipay/query_user_order_list', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestData)
            });
            
            const result = await response.json();
            
            if (result.code === '0000' && result.data) {
                this.renderOrderList(result.data.orderList, this.lastId === null);
                this.hasMore = result.data.hasMore;
                this.lastId = result.data.lastId;
                
                // 更新加载更多按钮状态
                this.updateLoadMoreButton();
            } else {
                this.showError('加载订单列表失败: ' + (result.info || '未知错误'));
            }
        } catch (error) {
            console.error('加载订单列表出错:', error);
            this.showError('网络错误，请稍后重试');
        } finally {
            this.loading = false;
            this.hideLoading();
        }
    }
    
    renderOrderList(orders, isFirstLoad = false) {
        const orderListElement = document.getElementById('orderList');
        const emptyStateElement = document.getElementById('emptyState');
        
        if (isFirstLoad) {
            orderListElement.innerHTML = '';
        }
        
        if (orders && orders.length > 0) {
            emptyStateElement.style.display = 'none';
            
            orders.forEach(order => {
                const orderElement = this.createOrderElement(order);
                orderListElement.appendChild(orderElement);
            });
        } else if (isFirstLoad) {
            emptyStateElement.style.display = 'block';
        }
    }
    
    createOrderElement(order) {
        const orderDiv = document.createElement('div');
        orderDiv.className = 'order-item';
        orderDiv.innerHTML = `
            <div class="order-header">
                <div class="order-id">订单号: ${order.orderId}</div>
                <div class="order-status status-${order.status}">${this.getStatusText(order.status)}</div>
            </div>
            <div class="order-content">
                <div class="product-name">${order.productName || '商品名称'}</div>
                <div class="order-details">
                    <div class="order-time">${this.formatTime(order.orderTime)}</div>
                    <div class="pay-amount">¥${order.payAmount || order.totalAmount}</div>
                </div>
            </div>
            <div class="order-actions">
                <button type="button" class="refund-btn" 
                        onclick="orderManager.showRefundModal('${order.orderId}')"
                        ${order.status === 'CLOSE' ? 'disabled' : ''}>
                    ${order.status === 'CLOSE' ? '已关闭' : '申请退单'}
                </button>
            </div>
        `;
        
        return orderDiv;
    }
    
    getStatusText(status) {
        const statusMap = {
            'CREATE': '新创建',
            'PAY_WAIT': '等待支付',
            'PAY_SUCCESS': '支付成功',
            'DEAL_DONE': '交易完成',
            'CLOSE': '已关闭'
        };
        return statusMap[status] || status;
    }
    
    formatTime(timeStr) {
        if (!timeStr) return '';
        const date = new Date(timeStr);
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    }
    
    updateLoadMoreButton() {
        const loadMoreBtn = document.getElementById('loadMoreBtn');
        if (this.hasMore) {
            loadMoreBtn.style.display = 'block';
            loadMoreBtn.disabled = false;
            loadMoreBtn.textContent = '加载更多';
        } else {
            loadMoreBtn.style.display = 'none';
        }
    }
    
    showRefundModal(orderId) {
        this.currentRefundOrderId = orderId;
        document.getElementById('refundModal').style.display = 'flex';
    }
    
    hideRefundModal() {
        document.getElementById('refundModal').style.display = 'none';
        this.currentRefundOrderId = null;
    }
    
    async processRefund() {
        if (!this.currentRefundOrderId) return;
        
        this.showLoading();
        
        try {
            const requestData = {
                userId: this.userId,
                orderId: this.currentRefundOrderId
            };
            
            const response = await fetch(AppConfig.sPayMallUrl + '/api/v1/alipay/refund_order', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestData)
            });
            
            const result = await response.json();
            
            if (result.code === '0000' && result.data && result.data.success) {
                this.showSuccess('退单成功');
                this.hideRefundModal();
                // 重新加载订单列表
                this.refreshOrderList();
            } else {
                this.showError('退单失败: ' + (result.info || result.data?.message || '未知错误'));
            }
        } catch (error) {
            console.error('退单操作出错:', error);
            this.showError('网络错误，请稍后重试');
        } finally {
            this.hideLoading();
        }
    }
    
    refreshOrderList() {
        this.lastId = null;
        this.hasMore = true;
        document.getElementById('orderList').innerHTML = '';
        this.loadOrderList();
    }
    
    showLoading() {
        document.getElementById('loadingTip').style.display = 'block';
    }
    
    hideLoading() {
        document.getElementById('loadingTip').style.display = 'none';
    }
    
    showError(message) {
        alert('错误: ' + message);
    }
    
    showSuccess(message) {
        alert('成功: ' + message);
    }
}

// 页面加载完成后初始化
let orderManager;
document.addEventListener('DOMContentLoaded', function() {
    orderManager = new OrderListManager();
});