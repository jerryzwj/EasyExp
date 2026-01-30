# 调整图片大小并替换应用图标

# 源图片路径
$sourceImage = "日常2_笔记本.png"

# 目标目录
$targetDirs = @(
    "android/app/src/main/res/mipmap-mdpi",
    "android/app/src/main/res/mipmap-hdpi",
    "android/app/src/main/res/mipmap-xhdpi",
    "android/app/src/main/res/mipmap-xxhdpi",
    "android/app/src/main/res/mipmap-xxxhdpi"
)

# 对应分辨率的尺寸
$sizes = @(
    48,
    72,
    96,
    144,
    192
)

# 加载System.Drawing命名空间
Add-Type -AssemblyName System.Drawing

# 处理每种分辨率
for ($i = 0; $i -lt $targetDirs.Length; $i++) {
    $targetDir = $targetDirs[$i]
    $size = $sizes[$i]
    
    # 确保目录存在
    if (-not (Test-Path $targetDir)) {
        New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    }
    
    # 目标文件路径
    $targetFile = Join-Path $targetDir "ic_launcher.png"
    $targetFileRound = Join-Path $targetDir "ic_launcher_round.png"
    
    try {
        # 加载图片
        $image = [System.Drawing.Image]::FromFile($sourceImage)
        
        # 创建新的缩略图
        $thumbnail = $image.GetThumbnailImage($size, $size, $null, [IntPtr]::Zero)
        
        # 保存为PNG
        $thumbnail.Save($targetFile, [System.Drawing.Imaging.ImageFormat]::Png)
        $thumbnail.Save($targetFileRound, [System.Drawing.Imaging.ImageFormat]::Png)
        
        # 释放资源
        $image.Dispose()
        $thumbnail.Dispose()
        
        Write-Host "已生成 $size x $size 图标: $targetFile"
        Write-Host "已生成 $size x $size 圆形图标: $targetFileRound"
    }
    catch {
        Write-Host "处理 $size x $size 图标时出错: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n图标替换完成！" -ForegroundColor Green