# LibraryJava

JavaFX library management application.

## Build Windows exe

From this folder:

```powershell
.\package-exe.ps1
```

If PowerShell says script execution is disabled, use:

```powershell
powershell -ExecutionPolicy Bypass -File .\package-exe.ps1
```

The runnable exe is created at:

```text
target\jpackage\LibraryJava\LibraryJava.exe
```

To create a Windows installer exe instead:

```powershell
.\package-exe.ps1 -Installer
```

The installer option requires the WiX Toolset to be installed and available on `PATH`.

The app still expects PostgreSQL to be running at `localhost:5432` with the database settings in `src/database/DatabaseConnection.java`.

## Online PostgreSQL

Create an online PostgreSQL database with Neon, Supabase, Railway, or another PostgreSQL host.

Run this SQL in the provider SQL editor:

```text
database/schema.sql
```

Then set these environment variables before starting the app:

```powershell
$env:LIBRARY_DB_URL="jdbc:postgresql://YOUR_HOST/YOUR_DATABASE?sslmode=require"
$env:LIBRARY_DB_USER="YOUR_USER"
$env:LIBRARY_DB_PASSWORD="YOUR_PASSWORD"
```

If these variables are not set, the app uses the local development database:

```text
jdbc:postgresql://localhost:5432/library
```
Team project in Object-Oriented Programming, Computer Programming 2 in Java.
Project specification: Library management system.
Team members: Abror, Inomjon, Nurshod, Ozodbek, Yusuf
