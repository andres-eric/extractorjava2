@echo off
echo ===================================================
echo     Actualizando repositorio local hacia Bitbucket
echo ===================================================
echo.

echo Agregando los archivos modificados (git add .)...
git add .
echo.

echo Mostrando el estado actual (git status)...
git status
echo.

set /p msg="Escribe una descripcion para estos cambios (Enter para usar una por defecto): "
if "%msg%"=="" set msg="Actualizacion desde Windows"

echo.
echo Guardando el commit...
git commit -m "%msg%"
echo.

echo Subiendo el codigo a Bitbucket (git push)...
git push
echo.

echo ===================================================
echo           Proceso terminado con exito!
echo ===================================================
pause
