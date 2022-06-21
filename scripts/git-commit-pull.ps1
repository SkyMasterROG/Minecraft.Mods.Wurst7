$Dbg = 0
if ($Dbg) {
    Write-Host "Hello World, $PSScriptRoot"
    Read-Host -Prompt "press [Enter] to exit"
    exit 1
}


# https://docs.gitlab.com/ee/gitlab-basics/start-using-git.html
# https://git-scm.com/docs/git-push
git commit -a -m "$(Get-Date -Format "yyyyMMdd-HHmm"): Update"
git push -u origin main

Read-Host -Prompt "press [Enter] to exit"
exit 0


#$argsList = "-NoExit -ExecutionPolicy Bypass -File .\Scripts\git-commit-pull.ps1"
#Start-Process powershell.exe -WorkingDirectory $(Get-Location) -Wait

$argsList = "-ExecutionPolicy Bypass -File .\Scripts\git-commit-pull.ps1"
Start-Process powershell.exe -WorkingDirectory $(Get-Location) -Wait -ArgumentList $argsList
