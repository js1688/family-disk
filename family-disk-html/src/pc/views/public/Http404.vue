<template>
  <n-result status="404" title="404 地址不存在" description="有想法可以提,但不要乱来.">
    <template #footer>
<!--      <n-button @click="">找点乐子吧</n-button>-->
    </template>
  </n-result>
</template>

<script>
import { NResult,NButton} from "naive-ui";
export default {
  name: "404",
  components:{NResult,NButton}
}
</script>

<style scoped>

</style>