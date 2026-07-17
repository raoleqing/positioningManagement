# 移动端 API 接口文档

> **Base URL**: `http://47.94.183.144:{port}/api`
> **Content-Type**: `application/json`
> **统一响应格式**: `{ "code": 200, "msg": "success", "data": ... }`

---

## 通用说明

| 项目 | 说明 |
|------|------|
| 成功码 | `code = 200` |
| 失败码 | `code = 500`，`msg` 为错误描述 |
| 开发端口 | `8010` |
| 生产端口 | `8080` |

---

## 1. 套餐模块

### 1.1 获取套餐列表

查询所有启用的套餐，供小程序端用户选择。

```
GET /mobile/package-plan/list
```

**请求参数**：无

**响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "planName": "基础套餐",
      "years": 1,
      "price": 9.90,
      "status": 1,
      "statusText": "启用",
      "sortOrder": 0,
      "remark": null,
      "createTime": "2024-01-01 12:00:00",
      "updateTime": "2024-01-01 12:00:00"
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 套餐ID |
| planName | String | 套餐名称 |
| years | Integer | 年数 |
| price | BigDecimal | 价格(元) |
| status | Integer | 0-停用, 1-启用 |
| statusText | String | 状态文字 |
| sortOrder | Integer | 排序值 |
| remark | String | 备注 |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

---

## 2. 充值模块

### 2.1 创建充值订单

```
POST /mobile/order/create
```

**请求体**：

```json
{
  "userId": 1,
  "userName": "张三",
  "deviceId": 123,
  "deviceNo": "IMEI123456789",
  "simCardNo": "89860000000000000000",
  "iccid": "89860000000000000000",
  "planId": 1,
  "remark": "备注"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | ✅ | 用户ID |
| userName | String | ✅ | 用户名称 |
| deviceId | Long | ✅ | 设备ID |
| deviceNo | String | ✅ | 设备编号 |
| simCardNo | String | 否 | SIM卡号 |
| iccid | String | ✅ | SIM卡唯一标识 |
| planId | Long | ✅ | 套餐ID |
| remark | String | 否 | 备注 |

**响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 100,
    "orderNo": "OD20240101120000001",
    "userId": 1,
    "userName": "张三",
    "deviceId": 123,
    "deviceNo": "IMEI123456789",
    "simCardNo": "89860000000000000000",
    "iccid": "89860000000000000000",
    "planId": 1,
    "planName": "基础套餐",
    "planAmount": 9.90,
    "payStatus": "UNPAID",
    "createTime": "2024-01-01 12:00:00"
  }
}
```

---

### 2.2 微信统一下单

获取小程序调起微信支付的参数。

```
POST /mobile/order/unified
```

**请求体**：

```json
{
  "orderId": "100",
  "openid": "oXXXX-xxxxxxxxxxx"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | String | ✅ | 订单ID |
| openid | String | 否 | 微信openid |

**响应示例**（微信支付小程序调起参数）：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "appId": "wx0000000000000000",
    "timeStamp": "1704096000",
    "nonceStr": "abc123def456",
    "package": "prepay_id=wx123456789",
    "signType": "RSA",
    "paySign": "XXXXXX..."
  }
}
```

---

### 2.3 充值记录查询（分页）

```
GET /mobile/recharge/records?userId=1&deviceId=123&payStatus=PAID&pageNum=1&pageSize=10
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | ✅ | 用户ID |
| deviceId | Long | 否 | 设备ID，按设备筛选 |
| payStatus | String | 否 | 支付状态：UNPAID / PAID / REFUND |
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页条数，默认 10 |

**响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 100,
        "orderNo": "OD20240101120000001",
        "deviceId": 123,
        "deviceNo": "IMEI123456789",
        "simCardNo": "89860000000000000000",
        "iccid": "89860000000000000000",
        "planId": 1,
        "planName": "基础套餐",
        "planAmount": 9.90,
        "planYears": 1,
        "payStatus": "PAID",
        "rechargeStatus": "SUCCESS",
        "notifyStatus": "NOTIFIED",
        "payTime": "2024-01-01 12:05:00",
        "rechargeTime": "2024-01-01 12:06:00",
        "createTime": "2024-01-01 12:00:00",
        "retryCount": 0,
        "errorMsg": null,
        "remark": null
      }
    ],
    "total": 25,
    "size": 10,
    "current": 1,
    "pages": 3
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单ID |
| orderNo | String | 订单号 |
| deviceId | Long | 设备ID |
| deviceNo | String | 设备编号 |
| simCardNo | String | SIM卡号 |
| iccid | String | SIM卡唯一标识 |
| planId | Long | 套餐ID |
| planName | String | 套餐名称 |
| planAmount | BigDecimal | 套餐金额 |
| planYears | Integer | 年数 |
| payStatus | String | 支付状态：UNPAID-未支付, PAID-已支付, REFUND-已退款 |
| rechargeStatus | String | 充值状态：PENDING-待充值, SUCCESS-成功, FAILED-失败 |
| notifyStatus | String | 通知状态 |
| payTime | DateTime | 支付时间 |
| rechargeTime | DateTime | 充值完成时间 |
| createTime | DateTime | 创建时间 |
| retryCount | Integer | 重试次数 |
| errorMsg | String | 错误信息 |
| remark | String | 备注 |

---

## 3. 支付回调（微信调用，非小程序端）

### 3.1 微信支付异步回调

由微信支付服务器调用，无需前端调用。需要在微信商户后台配置回调URL。

```
POST /mobile/pay/callback
```

**Headers**（微信签名，用于验签）：

| Header | 说明 |
|--------|------|
| Wechatpay-Signature | 微信签名 |
| Wechatpay-Serial | 平台证书序列号 |
| Wechatpay-Timestamp | 时间戳 |
| Wechatpay-Nonce | 随机字符串 |

**请求体**：微信支付回调JSON（加密的resource）

**响应**：`{"code": "SUCCESS", "message": "成功"}` 或 `{"code": "FAIL", "message": "失败"}`

---

### 3.2 模拟支付回调（仅开发环境）

```
POST /mobile/pay/mock-callback
```

**请求体**：

```json
{
  "orderNo": "OD20240101120000001"
}
```

---

## 4. 设备模块

### 4.1 用户设备列表

查询指定用户绑定的所有设备，返回设备摘要信息和主控标识。

```
GET /mobile/device/list?userId=1
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Integer | ✅ | 用户ID |

**响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "deviceId": 123,
      "deviceImei": "IMEI123456789012345",
      "deviceName": "车载GPS-01",
      "deviceType": 1,
      "deviceStatus": 1,
      "plate": "粤B12345",
      "deviceColour": "白色",
      "headPic": "https://xxx.com/head.jpg",
      "lastLongitude": 113.934569,
      "lastLatitude": 22.523100,
      "lastPositionDesc": "广东省深圳市南山区科技园",
      "lastLocationTime": "2024-01-01 12:00:00",
      "lastDeviceVol": "85%",
      "lastDeviceSms": "满格",
      "lastSpeed": 60.50,
      "lastMotionStatus": 1,
      "expireDate": "2025-01-01",
      "mainControl": 1,
      "relationShip": "本人"
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| deviceId | Integer | 设备ID |
| deviceImei | String | 设备唯一编号（IMEI） |
| deviceName | String | 设备名称 |
| deviceType | Integer | 设备类型 |
| deviceStatus | Integer | 1-启用, 0-禁用 |
| plate | String | 车牌号 |
| deviceColour | String | 设备颜色 |
| headPic | String | 头像URL |
| lastLongitude | BigDecimal | 最新经度 |
| lastLatitude | BigDecimal | 最新纬度 |
| lastPositionDesc | String | 最新地址描述 |
| lastLocationTime | DateTime | 最后定位时间 |
| lastDeviceVol | String | 电量 |
| lastDeviceSms | String | 信号强度 |
| lastSpeed | BigDecimal | 速度 |
| lastMotionStatus | Integer | 0-静止, 1-运动 |
| expireDate | Date | 有效期 |
| mainControl | Integer | 是否主控：0-一般用户, 1-主控 |
| relationShip | String | 用户与设备拥有者的关系 |

---

### 4.2 设备详情

查询单个设备的完整信息，包括附加属性、所有绑定用户。

```
GET /mobile/device/detail?deviceId=123
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| deviceId | Integer | ✅ | 设备ID |

**响应示例**：

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "deviceId": 123,
    "deviceImei": "IMEI123456789012345",
    "deviceName": "车载GPS-01",
    "bindMobile": "13800138000",
    "version": "V2.1.0",
    "deviceType": 1,
    "deviceStatus": 1,
    "plate": "粤B12345",
    "deviceColour": "白色",
    "headPic": "https://xxx.com/head.jpg",
    "organization": "一车队",
    "deviceShortNumber": "661",
    "deviceFamilyNumber": "123456",
    "expireDate": "2025-01-01",
    "deviceRemark": "测试设备",
    "deviceOwner": 1,
    "createTime": "2023-06-01 10:00:00",
    "updateTime": "2024-01-01 12:00:00",
    "lastLongitude": 113.934569,
    "lastLatitude": 22.523100,
    "lastPositionDesc": "广东省深圳市南山区科技园",
    "lastLocationTime": "2024-01-01 12:00:00",
    "lastLocationType": 0,
    "lastSpeed": 60.50,
    "accuracy": 5.20,
    "heading": 180.50,
    "altitude": 15.00,
    "lastMotionStatus": 1,
    "lastDeviceVol": "85%",
    "lastDeviceSms": "满格",
    "fuel": 50.50,
    "odometer": 123456.78,
    "baseMileage": 100.0,
    "timeZone": 480,
    "regularTimer": 30,
    "notifyStatus": 1,
    "ownerName": "张三",
    "additional": [
      { "key": "simNumber", "value": "89860000000000000000" },
      { "key": "deviceModel", "value": "GT06" }
    ],
    "bindUsers": [
      {
        "userId": 1,
        "userName": "张三",
        "mobile": "13800138000",
        "mainControl": 1,
        "relationShip": "本人"
      },
      {
        "userId": 2,
        "userName": "李四",
        "mobile": "13800138001",
        "mainControl": 0,
        "relationShip": "配偶"
      }
    ]
  }
}
```

**基本信息字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| deviceId | Integer | 设备ID |
| deviceImei | String | 设备唯一编号 |
| deviceName | String | 设备名称 |
| bindMobile | String | 绑定手机号 |
| version | String | 设备版本 |
| deviceType | Integer | 设备类型 |
| deviceStatus | Integer | 1-启用, 0-禁用 |
| plate | String | 车牌号 |
| deviceColour | String | 设备颜色 |
| headPic | String | 头像URL |
| organization | String | 所属车队 |
| deviceShortNumber | String | 短号 |
| deviceFamilyNumber | String | 家庭号码 |
| expireDate | Date | 有效期截止日 |
| deviceRemark | String | 备注 |
| deviceOwner | Long | 设备主人ID |
| createTime | DateTime | 创建时间 |
| updateTime | DateTime | 更新时间 |

**定位信息字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| lastLongitude | BigDecimal | 最新经度 |
| lastLatitude | BigDecimal | 最新纬度 |
| lastPositionDesc | String | 地址描述 |
| lastLocationTime | DateTime | 定位时间 |
| lastLocationType | Integer | 0-GPS, 1-WIFI, 2-基站 |
| lastSpeed | BigDecimal | 速度 |
| accuracy | BigDecimal | 定位精度(米) |
| heading | BigDecimal | 方向角(度) |
| altitude | BigDecimal | 海拔(米) |

**设备状态字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| lastMotionStatus | Integer | 0-静止, 1-运动 |
| lastDeviceVol | String | 电量 |
| lastDeviceSms | String | 信号强度 |
| fuel | BigDecimal | 油量 |
| odometer | BigDecimal | 里程(米) |
| baseMileage | Float | 基础里程 |

**配置字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| timeZone | Integer | 时区(分钟偏移) |
| regularTimer | Integer | 定位间隔(秒) |
| notifyStatus | Integer | 0-禁用通知, 1-启用通知 |

**关联信息字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| ownerName | String | 设备主人名称 |
| additional | List\<Map\> | 附加属性，每项含 `key` / `value` |
| bindUsers | List\<Object\> | 绑定用户列表 |

**bindUsers 子字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Integer | 用户ID |
| userName | String | 用户名称 |
| mobile | String | 手机号 |
| mainControl | Integer | 0-一般用户, 1-主控 |
| relationShip | String | 与设备拥有者的关系 |

---

## 接口汇总

| 序号 | 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|------|
| 1 | 套餐 | GET | `/mobile/package-plan/list` | 套餐列表 |
| 2 | 充值 | POST | `/mobile/order/create` | 创建充值订单 |
| 3 | 充值 | POST | `/mobile/order/unified` | 微信统一下单 |
| 4 | 充值 | GET | `/mobile/recharge/records` | 充值记录（分页） |
| 5 | 支付 | POST | `/mobile/pay/callback` | 微信支付回调 |
| 6 | 支付 | POST | `/mobile/pay/mock-callback` | 模拟回调(开发) |
| 7 | 设备 | GET | `/mobile/device/list` | 用户设备列表 |
| 8 | 设备 | GET | `/mobile/device/detail` | 设备详情 |
