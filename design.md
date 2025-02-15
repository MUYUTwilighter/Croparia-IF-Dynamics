# 矿石魔种：元素动力（Croparia IF: Dynamics）企划

## 艾灵网络 Elenet | 已完成

受 *戴森球计划* 的无线能量传输机制启发，以元素流体为主要消耗能源，通过无线连接方式（**共鸣 resonance**）将 *艾灵终端*
连接起来进行物流交互。

- 需要定义两种设备接口：*艾灵枢纽*、*艾灵终端*，它们是 *艾灵网络* 的重要组成部分。
- 内置一个表，来存储加入到该网络的所有 *艾灵终端*。
- 记录一个 **元素刻印 engrave**，用于分辨不同的网络。（尽管内存地址判断可用，但是要考虑到序列化与反序列化）
- 记录一个 @Nullable **元素令牌 token**，具有相同 *元素令牌* 的 *艾灵枢纽* 才可以加入并扩展该网络。定义了 *元素令牌*
  的网络不会自动删除。
- 由于会涉及频繁的区域检索与高复杂度算法，可能需要多线程优化。
- 艾灵网络的速度不受 *艾灵枢纽* 的影响，而是由 *艾灵终端* 决定。

### 艾灵终端 Elenet Peer

艾灵网络的服务对象，物流的主动发起方，但在网络拓扑上是被动的。

需要开放 Repo API 接口来让网络处理物流请求。

以方块面为基本连接单位，每个单位对一种物流接口只能通信一个。（比如，一个机器方块的顶部最多仅能同时有一个流体接口和一个物品接口）

重要行为：
> - 请求与接收：需要先检测连接的 *艾灵枢纽* 是否可用，然后开放 Repo API 接口给它进行处理。

#### 艾灵接口

对于非本模组的机器，可以使用 *艾灵接口* 来加入到 *艾灵网络*。它在网络中被视为 *艾灵终端* 的一种，通过流体或物品交互接口（Repo
API）与其他机器交互，同时实现了 *艾灵终端* 来与 *艾灵网络* 进行通信。

### 艾灵枢纽 Elenet HUB

用于创建 ***艾灵网络覆盖域 Coverage***，在覆盖域中的 *艾灵终端* 之间的交互需要向其连接到的 *艾灵枢纽* 发起 / 接收物流请求。

*艾灵枢纽* 之间允许 ***桥接***，用于扩大 *覆盖域*。

小覆盖域设备处于大覆盖域设备范围内，应该认为可构成双向连接。

此外，为了更好地实现桥接，*艾灵枢纽* 会在比 *覆盖域* 更大地范围（默认是其半径的两倍 + 1）内搜索其他 *艾灵枢纽*。

重要行为：
> - 创建（放置）时：
>> - 扫描一定区域的 *艾灵枢纽*
>>> - 如果发现多个设备且处于 **不同网络**，则合并网络
>>> - 如果发现多个设备且都处于 **同一网络**，则将本设备连接到最近的设备以加入该网络
>>> - 如果未发现任何网络，则创建一个新网络
>> - 扫描一定区域尚未构成连接的 *艾灵终端*
>>> 1. 尝试将其连接
>>> 2. 将其记录在当前网络
>> - 创建后，间歇执行上述扫描行为，以避免 *覆盖域* 大小不一造成的判断错误。
> - 处理请求：向网络记录的所有 *艾灵终端* 一一轮询
>> - 先在当前覆盖域中轮询
>> - 然后在整个网络中轮询
> - 移除（摧毁）时：
>> 1. 将当前网络挂起，暂不处理任何请求，但是缓存。
>> 2. 更新连接到本设备的 *艾灵终端* 状态，让触发它们的连接刷新。
>> 3. 将邻居节点全部划分为不同的子网。
>> 4. 如果在处理过程中发生了其他与当前网络拓扑相关的事件（如其他 *艾灵枢纽* 的创建或移除），则将这些事件也挂起，等待当前事件完成后再处理。

## 元素锻仪 Elemental Forge | 设计中

*元素锻仪* 是对本附属各类机器的抽象，它默认应该实现 ElenetPeerProvider 接口，但初始状态下无法访问 *艾灵网络*，它需要安装对应的
*艾灵星盘* 才可以被 *艾灵枢纽* 检索到。它应该默认实现例如管道的方块面物流交互。

*元素锻仪* 需要 *核心炉* 与 *元素流体*（可以视为燃料） 才能工作。

*元素锻仪* 可以通过 *元素框架 Elemental Frame* 升级，越高的等级具有更多的并行产线（类似 Mekanism）。

### 核心炉 Elemental Crucible

一个放置在 *元素锻仪* 中的一个物品，用于决定 *元素锻仪* 对 *元素流体* 的兼容性。

一共五个等级，越高的等级能够兼容的 *元素流体* 越多。这个效果参照信标的 buff 选择。

### 元素流体 Elemental Fluid

由 Croparia IF 本体定义的，是五种元素物品的流体形式。在本附属中，它可以作为仪器的“燃料”。不同的 *元素流体* 对仪器有不同的效果。

- 魔力：最普通的“燃料”；仪器在默认状态下会消耗它。
- 土：有概率减少仪器处理的物品损耗；仪器只会在触发其效果时消耗它，越多的土元素流体触发概率越大。
- 水：有概率减少仪器处理的流体损耗；仪器只会在触发其效果时消耗它，越多的水元素流体触发概率越大。
- 火：加快仪器处理速度；仪器不会损耗它，但越多的火元素流体其效果越明显。
- 风：减少仪器的能耗；仪器不会损耗它，但越多的风元素流体其效果越明显。

对于土元素和水元素的效果，考虑到材料可能出现“循环增益”的情况，它们的效果仅在配方中启用时才能生效。

### 艾灵星盘 Elenet Astrolabe

用于控制 *元素锻仪* 对各种 *艾灵网络* 的访问。

- 星盘：物品
- 星盘：流体

