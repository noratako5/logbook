package logbook.extraLog

import com.dyuproject.protostuff.LinkedBuffer

private val buffer = LinkedBuffer.allocate(128 * 1024)

//出撃時のjsonを一通り保存できるようにする予定だったらしいが何してたのか思い出せないので一旦放置